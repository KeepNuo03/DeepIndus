import base64
import os
import time
from pathlib import Path
from typing import Optional

import cv2
import numpy as np
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from ultralytics import YOLO

app = FastAPI(title="YOLO Detection Service")

# CORS (development)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def _env_bool(name: str, default: bool) -> bool:
    raw = os.getenv(name)
    if raw is None:
        return default
    return raw.strip().lower() in {"1", "true", "yes", "y", "on"}


def _env_int(name: str, default: int) -> int:
    raw = os.getenv(name)
    if raw is None:
        return default
    try:
        return int(raw.strip())
    except Exception:
        return default


def _env_float(name: str, default: float) -> float:
    raw = os.getenv(name)
    if raw is None:
        return default
    try:
        return float(raw.strip())
    except Exception:
        return default


# Runtime config (TensorRT landing checklist aligned)
YOLO_BACKEND = os.getenv("YOLO_BACKEND", "auto").strip().lower()  # auto|pytorch|onnx|tensorrt
YOLO_DEVICE = os.getenv("YOLO_DEVICE", "0").strip()  # 0/cuda:0/cpu
YOLO_CONF_THRESHOLD = _env_float("YOLO_CONF_THRESHOLD", 0.2)
YOLO_IOU_THRESHOLD = _env_float("YOLO_IOU_THRESHOLD", 0.45)
YOLO_MAX_DET = _env_int("YOLO_MAX_DET", 300)
YOLO_IMGSZ = _env_int("YOLO_IMGSZ", 640)
YOLO_INCLUDE_ANNOTATED_DEFAULT = _env_bool("YOLO_INCLUDE_ANNOTATED", True)
YOLO_USE_FP16 = _env_bool("YOLO_USE_FP16", True)
YOLO_WARMUP = _env_bool("YOLO_WARMUP", True)
YOLO_VERBOSE = _env_bool("YOLO_VERBOSE", False)
YOLO_BACKEND_FALLBACK = _env_bool("YOLO_BACKEND_FALLBACK", True)

YOLO_MODEL_PT_PATH = Path(os.getenv("YOLO_MODEL_PT_PATH", "models/best.pt"))
YOLO_MODEL_ONNX_PATH = Path(os.getenv("YOLO_MODEL_ONNX_PATH", "models/best.onnx"))
YOLO_MODEL_ENGINE_PATH = Path(os.getenv("YOLO_MODEL_ENGINE_PATH", "models/best.engine"))

model: Optional[YOLO] = None
runtime_backend = "unknown"
runtime_model_path = ""


def _is_gpu_device(device: str) -> bool:
    d = (device or "").strip().lower()
    return d not in {"", "cpu"}


def _select_model_path() -> tuple[str, Path]:
    # Auto strategy: TensorRT engine > ONNX > PyTorch.
    # This keeps rollback simple while maximizing GPU acceleration.
    if YOLO_BACKEND == "tensorrt":
        return "tensorrt", YOLO_MODEL_ENGINE_PATH
    if YOLO_BACKEND == "onnx":
        return "onnx", YOLO_MODEL_ONNX_PATH
    if YOLO_BACKEND == "pytorch":
        return "pytorch", YOLO_MODEL_PT_PATH

    if YOLO_MODEL_ENGINE_PATH.exists() and _is_gpu_device(YOLO_DEVICE):
        return "tensorrt", YOLO_MODEL_ENGINE_PATH
    if YOLO_MODEL_ONNX_PATH.exists():
        return "onnx", YOLO_MODEL_ONNX_PATH
    return "pytorch", YOLO_MODEL_PT_PATH


def _load_with_fallback() -> tuple[YOLO, str, Path]:
    backend, path = _select_model_path()
    if path.exists():
        return YOLO(str(path)), backend, path

    if not YOLO_BACKEND_FALLBACK:
        raise RuntimeError(f"Configured backend={backend}, model file not found: {path}")

    # Fallback chain for robustness
    for b, p in [
        ("tensorrt", YOLO_MODEL_ENGINE_PATH),
        ("onnx", YOLO_MODEL_ONNX_PATH),
        ("pytorch", YOLO_MODEL_PT_PATH),
    ]:
        if p.exists():
            return YOLO(str(p)), b, p
    raise RuntimeError(
        "No model file found. Checked: "
        f"{YOLO_MODEL_ENGINE_PATH}, {YOLO_MODEL_ONNX_PATH}, {YOLO_MODEL_PT_PATH}"
    )


@app.on_event("startup")
def load_model() -> None:
    global model, runtime_backend, runtime_model_path
    loaded_model, backend, model_path = _load_with_fallback()
    model = loaded_model
    runtime_backend = backend
    runtime_model_path = str(model_path)

    if YOLO_WARMUP:
        warmup_img = np.zeros((YOLO_IMGSZ, YOLO_IMGSZ, 3), dtype=np.uint8)
        _ = model.predict(
            source=warmup_img,
            conf=YOLO_CONF_THRESHOLD,
            iou=YOLO_IOU_THRESHOLD,
            max_det=YOLO_MAX_DET,
            imgsz=YOLO_IMGSZ,
            device=YOLO_DEVICE,
            half=(YOLO_USE_FP16 and runtime_backend == "pytorch"),
            verbose=False,
        )

    print(
        "[YOLO] model loaded",
        {
            "backend": runtime_backend,
            "path": runtime_model_path,
            "device": YOLO_DEVICE,
            "imgsz": YOLO_IMGSZ,
            "conf": YOLO_CONF_THRESHOLD,
            "iou": YOLO_IOU_THRESHOLD,
            "fp16": YOLO_USE_FP16,
        },
    )


@app.get("/health")
def health():
    return {
        "status": "ok",
        "model_loaded": model is not None,
        "backend": runtime_backend,
        "model_path": runtime_model_path,
        "device": YOLO_DEVICE,
    }


def _decode_image(contents: bytes) -> np.ndarray:
    nparr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if img is None:
        raise HTTPException(status_code=400, detail="Invalid image input")
    return img


def _run_inference(img: np.ndarray):
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")
    return model.predict(
        source=img,
        conf=YOLO_CONF_THRESHOLD,
        iou=YOLO_IOU_THRESHOLD,
        max_det=YOLO_MAX_DET,
        imgsz=YOLO_IMGSZ,
        device=YOLO_DEVICE,
        half=(YOLO_USE_FP16 and runtime_backend == "pytorch"),
        verbose=YOLO_VERBOSE,
    )


@app.post("/detect/image")
async def detect_image(
    file: UploadFile = File(...),
    include_annotated: Optional[bool] = None,
):
    contents = await file.read()
    img = _decode_image(contents)

    start = time.time()
    results = _run_inference(img)
    inference_time = (time.time() - start) * 1000

    detections = []
    for r in results:
        for box in r.boxes:
            cls_idx = int(box.cls[0])
            detections.append(
                {
                    "class_id": cls_idx,
                    "class_name": model.names[cls_idx],
                    "confidence": float(box.conf[0]),
                    "bbox": {
                        "x1": float(box.xyxy[0][0]),
                        "y1": float(box.xyxy[0][1]),
                        "x2": float(box.xyxy[0][2]),
                        "y2": float(box.xyxy[0][3]),
                    },
                }
            )

    should_render_annotated = (
        YOLO_INCLUDE_ANNOTATED_DEFAULT if include_annotated is None else include_annotated
    )
    annotated_data_url = None
    if should_render_annotated and results:
        annotated_img = results[0].plot()
        ok, buffer = cv2.imencode(".jpg", annotated_img)
        if ok:
            img_base64 = base64.b64encode(buffer).decode("utf-8")
            annotated_data_url = f"data:image/jpeg;base64,{img_base64}"

    return {
        "code": 200,
        "data": {
            "detections": detections,
            "detection_count": len(detections),
            "inference_time_ms": round(inference_time, 2),
            "backend": runtime_backend,
            "model_path": runtime_model_path,
            "annotated_image": annotated_data_url,
        },
    }


@app.post("/detect/video-frame")
async def detect_video_frame(
    file: UploadFile = File(...),
    include_annotated: Optional[bool] = None,
):
    return await detect_image(file=file, include_annotated=include_annotated)


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8001)