# YOLO Service (TensorRT-ready)

This service provides image/frame detection APIs for DeepIndus.

## Endpoints

- `GET /health`
- `POST /detect/image`
- `POST /detect/video-frame`

## TensorRT Acceleration Strategy

- Keep upper API unchanged.
- Runtime switch by env: `YOLO_BACKEND=auto|pytorch|onnx|tensorrt`
- `auto` priority: TensorRT engine -> ONNX -> PyTorch.
- Supports FP16 for PyTorch backend (`YOLO_USE_FP16=true`).
- Supports warmup on startup (`YOLO_WARMUP=true`).

## Quick Start

```powershell
cd C:\deepIndus\yolo-service
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python main.py
```

## Env Configuration

Copy `.env.example` and set values according to your environment.

Key vars:
- `YOLO_BACKEND`
- `YOLO_DEVICE`
- `YOLO_MODEL_*_PATH`
- `YOLO_CONF_THRESHOLD`
- `YOLO_IMGSZ`

## Validation Checklist

1. `/health` shows selected backend and model path.
2. `POST /detect/image` returns detection result.
3. Compare baseline vs TensorRT:
   - latency P95
   - throughput
   - stability
4. If issue occurs, rollback by setting:
   - `YOLO_BACKEND=pytorch`
