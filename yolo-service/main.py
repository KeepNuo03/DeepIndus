from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from ultralytics import YOLO
import cv2
import numpy as np
import time
import base64

app = FastAPI(title="YOLO Detection Service")

# 允许跨域（开发环境）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

model = None
CONF_THRESHOLD = 0.2  # 降低阈值，减少漏检

@app.on_event("startup")
def load_model():
    global model
    model = YOLO(r"models/best.pt")   # 注意路径是相对 C:\yolo-service
    print("YOLO model loaded. Classes:", model.names)

@app.get("/health")
def health():
    return {"status": "ok", "model_loaded": model is not None}

@app.post("/detect/image")
async def detect_image(file: UploadFile = File(...)):
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")

    contents = await file.read()
    nparr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    start = time.time()
    results = model(img, conf=CONF_THRESHOLD)
    inference_time = (time.time() - start) * 1000

    detections = []
    for r in results:
        for box in r.boxes:
            detections.append({
                "class_id": int(box.cls[0]),
                "class_name": model.names[int(box.cls[0])],
                "confidence": float(box.conf[0]),
                "bbox": {
                    "x1": float(box.xyxy[0][0]),
                    "y1": float(box.xyxy[0][1]),
                    "x2": float(box.xyxy[0][2]),
                    "y2": float(box.xyxy[0][3]),
                }
            })

    # 生成带框标注图（供前端在主监控/视频流区域显示）
    annotated_img = results[0].plot()
    _, buffer = cv2.imencode('.jpg', annotated_img)
    img_base64 = base64.b64encode(buffer).decode('utf-8')

    print(f"Detections: {len(detections)}")
    return {
        "code": 200,
        "data": {
            "detections": detections,
            "detection_count": len(detections),
            "inference_time_ms": round(inference_time, 2),
            "annotated_image": f"data:image/jpeg;base64,{img_base64}"
        }
    }

@app.post("/detect/video-frame")
async def detect_video_frame(file: UploadFile = File(...)):
    # 用于实时检测
    return await detect_image(file)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)