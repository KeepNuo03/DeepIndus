# YOLO 算法集成指南 - Vue + Spring Boot 架构

## 🎯 架构方案

### 整体架构设计

```
┌─────────────┐     HTTP/WebSocket    ┌──────────────┐     HTTP     ┌─────────────┐
│             │ ──────────────────────>│              │ ───────────> │             │
│  Vue 前端   │                        │ Spring Boot  │              │  YOLO 服务  │
│             │ <──────────────────────│   后端 API   │ <─────────── │  (Python)   │
└─────────────┘                        └──────────────┘              └─────────────┘
      │                                      │                              │
      │                                      │                              │
      v                                      v                              v
  显示结果                              MySQL + Redis                    GPU 推理
```

---

## ⭐ 推荐方案：Python 微服务架构

### 方案优势
- ✅ **性能最优** - Python 直接调用 YOLO 模型
- ✅ **职责清晰** - Spring Boot 管业务，Python 管 AI
- ✅ **易于维护** - AI 模型独立部署和更新
- ✅ **可扩展** - 支持多个 AI 服务

---

## 🏗️ 详细实施方案

### 第一步：搭建 YOLO 推理服务（Python + FastAPI）

#### 1.1 创建 Python 项目

```bash
# 创建项目目录
mkdir yolo-service
cd yolo-service

# 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 安装依赖
pip install fastapi uvicorn opencv-python torch torchvision ultralytics pillow numpy
```

#### 1.2 创建 YOLO 推理服务

**文件：** `yolo-service/main.py`

```python
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from ultralytics import YOLO
import cv2
import numpy as np
from PIL import Image
import io
import base64
from typing import List, Dict
import time

app = FastAPI(title="YOLO Detection Service")

# CORS 配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境改为具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 加载 YOLO 模型（启动时加载）
model = None

@app.on_event("startup")
async def load_model():
    global model
    print("Loading YOLO model...")
    # 加载你训练好的模型
    model = YOLO('models/best.pt')  # 你的模型路径
    print("Model loaded successfully!")

@app.get("/")
def root():
    return {"service": "YOLO Detection Service", "status": "running"}

@app.get("/health")
def health_check():
    """健康检查"""
    return {
        "status": "healthy",
        "model_loaded": model is not None
    }

@app.post("/detect/image")
async def detect_image(file: UploadFile = File(...)):
    """
    单张图片检测
    """
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    try:
        # 读取图片
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        # YOLO 推理
        start_time = time.time()
        results = model(img)
        inference_time = (time.time() - start_time) * 1000  # ms
        
        # 解析结果
        detections = []
        for result in results:
            boxes = result.boxes
            for box in boxes:
                detection = {
                    "class_id": int(box.cls[0]),
                    "class_name": model.names[int(box.cls[0])],
                    "confidence": float(box.conf[0]),
                    "bbox": {
                        "x1": float(box.xyxy[0][0]),
                        "y1": float(box.xyxy[0][1]),
                        "x2": float(box.xyxy[0][2]),
                        "y2": float(box.xyxy[0][3])
                    }
                }
                detections.append(detection)
        
        # 生成标注图片（可选）
        annotated_img = results[0].plot()
        _, buffer = cv2.imencode('.jpg', annotated_img)
        img_base64 = base64.b64encode(buffer).decode('utf-8')
        
        return {
            "code": 200,
            "message": "Detection completed",
            "data": {
                "detections": detections,
                "detection_count": len(detections),
                "inference_time_ms": round(inference_time, 2),
                "image_size": {
                    "width": img.shape[1],
                    "height": img.shape[0]
                },
                "annotated_image": f"data:image/jpeg;base64,{img_base64}"
            }
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/detect/video-frame")
async def detect_video_frame(file: UploadFile = File(...)):
    """
    视频帧检测（用于实时监控）
    """
    # 同上，但不返回 base64 图片（节省带宽）
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    try:
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        start_time = time.time()
        results = model(img)
        inference_time = (time.time() - start_time) * 1000
        
        detections = []
        for result in results:
            boxes = result.boxes
            for box in boxes:
                detection = {
                    "class_id": int(box.cls[0]),
                    "class_name": model.names[int(box.cls[0])],
                    "confidence": float(box.conf[0]),
                    "bbox": {
                        "x1": float(box.xyxy[0][0]),
                        "y1": float(box.xyxy[0][1]),
                        "x2": float(box.xyxy[0][2]),
                        "y2": float(box.xyxy[0][3])
                    }
                }
                detections.append(detection)
        
        return {
            "code": 200,
            "data": {
                "detections": detections,
                "detection_count": len(detections),
                "inference_time_ms": round(inference_time, 2),
                "has_defect": len(detections) > 0
            }
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/model/info")
def get_model_info():
    """
    获取模型信息
    """
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    return {
        "code": 200,
        "data": {
            "model_type": "YOLOv8",
            "classes": model.names,
            "class_count": len(model.names)
        }
    }

if __name__ == "__main__":
    import uvicorn
    # 启动服务
    uvicorn.run(app, host="0.0.0.0", port=8001)
```

#### 1.3 启动 YOLO 服务

```bash
# 确保模型文件在正确位置
# yolo-service/models/best.pt

# 启动服务
python main.py

# 服务将运行在 http://localhost:8001
```

#### 1.4 测试 YOLO 服务

```bash
# 健康检查
curl http://localhost:8001/health

# 测试图片检测
curl -X POST "http://localhost:8001/detect/image" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@test_image.jpg"
```

---

### 第二步：Spring Boot 调用 YOLO 服务

#### 2.1 添加依赖

**文件：** `pom.xml`

```xml
<!-- HTTP 客户端 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- 或使用 OkHttp -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

#### 2.2 创建 YOLO 服务调用类

**文件：** `src/main/java/com/induscore/service/YoloService.java`

```java
package com.induscore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class YoloService {

    @Value("${yolo.service.url:http://localhost:8001}")
    private String yoloServiceUrl;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YoloService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 检测单张图片
     */
    public DetectionResult detectImage(MultipartFile file) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), 
                        MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(yoloServiceUrl + "/detect/image")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("YOLO service error: " + response.code());
            }

            String responseBody = response.body().string();
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            
            return parseDetectionResult(result);
        }
    }

    /**
     * 检测视频帧（用于实时监控）
     */
    public DetectionResult detectVideoFrame(byte[] frameData) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "frame.jpg",
                        RequestBody.create(frameData, MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(yoloServiceUrl + "/detect/video-frame")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("YOLO detection failed: {}", response.code());
                return null;
            }

            String responseBody = response.body().string();
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            
            return parseDetectionResult(result);
        } catch (Exception e) {
            log.error("YOLO detection error", e);
            return null;
        }
    }

    /**
     * 解析检测结果
     */
    private DetectionResult parseDetectionResult(Map<String, Object> result) {
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        
        DetectionResult detection = new DetectionResult();
        detection.setDetections((List<Map<String, Object>>) data.get("detections"));
        detection.setDetectionCount((Integer) data.get("detection_count"));
        detection.setInferenceTimeMs((Double) data.get("inference_time_ms"));
        detection.setHasDefect((Boolean) data.getOrDefault("has_defect", false));
        
        return detection;
    }

    /**
     * 健康检查
     */
    public boolean checkHealth() {
        try {
            Request request = new Request.Builder()
                    .url(yoloServiceUrl + "/health")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.error("YOLO service health check failed", e);
            return false;
        }
    }
}
```

#### 2.3 创建检测结果模型

**文件：** `src/main/java/com/induscore/model/DetectionResult.java`

```java
package com.induscore.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DetectionResult {
    private List<Map<String, Object>> detections;  // 检测到的缺陷列表
    private Integer detectionCount;                 // 检测数量
    private Double inferenceTimeMs;                 // 推理耗时
    private Boolean hasDefect;                      // 是否有缺陷
    private Map<String, Object> imageSize;          // 图片尺寸
    private String annotatedImage;                  // 标注后的图片（base64）
}
```

#### 2.4 创建检测控制器

**文件：** `src/main/java/com/induscore/controller/DetectionController.java`

```java
package com.induscore.controller;

import com.induscore.service.YoloService;
import com.induscore.service.DetectionRecordService;
import com.induscore.model.DetectionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/detection")
public class DetectionController {

    @Autowired
    private YoloService yoloService;

    @Autowired
    private DetectionRecordService recordService;

    /**
     * 上传图片进行检测
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadAndDetect(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "serialNo", required = false) String serialNo,
            @RequestParam(value = "productId", required = false) Long productId) {
        
        try {
            // 1. 调用 YOLO 服务进行检测
            DetectionResult result = yoloService.detectImage(file);
            
            // 2. 保存检测记录到数据库
            if (result.getHasDefect()) {
                // 有缺陷，保存记录
                recordService.saveDetectionRecord(result, serialNo, productId, file);
            }
            
            // 3. 返回结果给前端
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "检测完成");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Detection failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "检测失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 检查 YOLO 服务健康状态
     */
    @GetMapping("/service/health")
    public ResponseEntity<Map<String, Object>> checkYoloHealth() {
        boolean healthy = yoloService.checkHealth();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of(
            "yolo_service_status", healthy ? "online" : "offline",
            "timestamp", System.currentTimeMillis()
        ));
        
        return ResponseEntity.ok(response);
    }
}
```

---

### 第三步：实时视频流检测

#### 3.1 视频流处理方案

**方案 A：WebSocket 实时推送**

**Spring Boot 端：**

```java
package com.induscore.websocket;

import com.induscore.service.YoloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;

@Slf4j
@Component
public class VideoStreamHandler extends BinaryWebSocketHandler {

    @Autowired
    private YoloService yoloService;

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            // 接收视频帧
            byte[] frameData = message.getPayload().array();
            
            // 调用 YOLO 检测
            DetectionResult result = yoloService.detectVideoFrame(frameData);
            
            // 如果检测到缺陷，推送给前端
            if (result != null && result.getHasDefect()) {
                String jsonResult = objectMapper.writeValueAsString(result);
                session.sendMessage(new TextMessage(jsonResult));
            }
            
        } catch (Exception e) {
            log.error("Video frame detection error", e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket disconnected: {}", session.getId());
    }
}
```

**WebSocket 配置：**

```java
package com.induscore.config;

import com.induscore.websocket.VideoStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private VideoStreamHandler videoStreamHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(videoStreamHandler, "/ws/video-stream")
                .setAllowedOrigins("*");  // 生产环境改为具体域名
    }
}
```

---

#### 3.2 摄像头视频流接入

**方案：使用 RTSP/HTTP 流**

```java
package com.induscore.service;

import org.bytedeco.javacv.*;
import org.springframework.stereotype.Service;

@Service
public class CameraStreamService {

    /**
     * 从 RTSP 流读取视频帧
     */
    public void startCapture(String rtspUrl, VideoFrameCallback callback) {
        new Thread(() -> {
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl)) {
                grabber.start();
                
                Frame frame;
                while ((frame = grabber.grabFrame()) != null) {
                    if (frame.image != null) {
                        // 转换为 byte[]
                        byte[] frameData = frameToBytes(frame);
                        
                        // 回调处理
                        callback.onFrame(frameData);
                    }
                }
                
                grabber.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private byte[] frameToBytes(Frame frame) {
        // 实现 Frame 到 byte[] 的转换
        // 使用 OpenCV 或 JavaCV 工具
        return new byte[0];  // 简化示例
    }
}

interface VideoFrameCallback {
    void onFrame(byte[] frameData);
}
```

---

### 第四步：前端调用实时检测

#### 4.1 前端 WebSocket 连接

**前端已有的 WebSocket 工具：** `src/utils/websocket.js`（参考 FRONTEND_API_INTEGRATION.md）

**在实时检测页面使用：**

```javascript
// src/pages/real_time_detect.vue

import { wsClient } from '@/utils/websocket';

export default {
  mounted() {
    const token = localStorage.getItem('token');
    
    // 连接 WebSocket
    wsClient.connect(token);
    
    // 监听检测结果
    wsClient.on('detection', (data) => {
      console.log('收到检测结果', data);
      
      // 更新页面
      this.addDetectionResult(data);
      
      // 如果有缺陷，显示报警
      if (data.has_defect) {
        this.showAlert(data);
      }
    });
  },
  
  methods: {
    addDetectionResult(data) {
      // 添加到检测记录列表
      this.recentRecords.unshift({
        type: data.has_defect ? 'warning' : 'normal',
        title: data.detections[0]?.class_name || '正常',
        confidence: data.detections[0]?.confidence || 100,
        timestamp: new Date().toLocaleTimeString()
      });
      
      // 更新统计数据
      this.todayTargets++;
      if (data.has_defect) {
        this.warningCount++;
      }
    }
  }
};
```

---

## 🔄 完整数据流转

### 离线检测（上传图片）

```
1. 用户在前端上传图片
   ↓
2. 前端 → POST /api/v1/detection/upload → Spring Boot
   ↓
3. Spring Boot → POST /detect/image → YOLO 服务（Python）
   ↓
4. YOLO 推理并返回结果 → Spring Boot
   ↓
5. Spring Boot 保存到数据库 + 返回结果 → 前端
   ↓
6. 前端显示检测结果和标注图片
```

### 实时检测（视频流）

```
1. 摄像头推送 RTSP 视频流
   ↓
2. Spring Boot 读取视频帧
   ↓
3. Spring Boot → POST /detect/video-frame → YOLO 服务
   ↓
4. YOLO 推理返回结果 → Spring Boot
   ↓
5. Spring Boot 通过 WebSocket 推送 → 前端
   ↓
6. 前端实时显示检测框和报警
```

---

## 🚀 部署方案

### 开发环境

```
端口分配：
- 前端：http://localhost:5173 (Vite)
- Spring Boot：http://localhost:8000
- YOLO 服务：http://localhost:8001
- MySQL：localhost:3306
- Redis：localhost:6379
```

### 生产环境

**使用 Docker Compose 编排：**

```yaml
# docker-compose.yml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: induscore
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  # Redis 缓存
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  # YOLO 推理服务（Python）
  yolo-service:
    build: ./yolo-service
    ports:
      - "8001:8001"
    volumes:
      - ./models:/app/models
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: 1
              capabilities: [gpu]  # GPU 支持

  # Spring Boot 后端
  backend:
    build: ./backend
    ports:
      - "8000:8000"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/induscore
      SPRING_REDIS_HOST: redis
      YOLO_SERVICE_URL: http://yolo-service:8001
    depends_on:
      - mysql
      - redis
      - yolo-service

  # Nginx 前端
  frontend:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - backend

volumes:
  mysql_data:
```

---

## ⚙️ 配置文件

### Spring Boot 配置

**文件：** `application.yml`

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB      # 最大上传文件大小
      max-request-size: 50MB
  
  datasource:
    url: jdbc:mysql://localhost:3306/induscore
    username: root
    password: root123

# YOLO 服务配置
yolo:
  service:
    url: http://localhost:8001
    timeout: 30000  # 30秒超时

# 文件存储配置
file:
  upload:
    path: /data/uploads/
    base-url: http://localhost:8000/files/
```

### YOLO 服务配置

**文件：** `yolo-service/config.py`

```python
class Config:
    # 模型配置
    MODEL_PATH = 'models/best.pt'
    
    # 检测配置
    CONFIDENCE_THRESHOLD = 0.5  # 置信度阈值
    IOU_THRESHOLD = 0.45        # NMS IOU 阈值
    
    # 服务配置
    HOST = '0.0.0.0'
    PORT = 8001
    
    # GPU 配置
    DEVICE = 'cuda:0'  # 使用 GPU，如果没有 GPU 改为 'cpu'
```

---

## 📊 性能优化

### YOLO 服务优化

**1. 使用 GPU 加速**
```python
# 加载模型时指定 GPU
model = YOLO('models/best.pt')
model.to('cuda')  # 移到 GPU
```

**2. 批量检测**
```python
# 批量处理多张图片
results = model([img1, img2, img3], stream=True)
```

**3. 使用半精度**
```python
# FP16 半精度推理（速度快 2 倍）
results = model(img, half=True)
```

### Spring Boot 优化

**1. 使用连接池**
```java
OkHttpClient httpClient = new OkHttpClient.Builder()
    .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
    .build();
```

**2. 异步调用**
```java
@Async
public CompletableFuture<DetectionResult> detectImageAsync(MultipartFile file) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            return detectImage(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
}
```

**3. Redis 缓存结果**
```java
@Cacheable(value = "detections", key = "#fileHash")
public DetectionResult detectImage(String fileHash, MultipartFile file) {
    // ...
}
```

---

## 🧪 测试流程

### 1. 测试 YOLO 服务

```bash
# 启动 YOLO 服务
cd yolo-service
python main.py

# 测试接口
curl -X POST http://localhost:8001/detect/image \
  -F "file=@test.jpg"
```

### 2. 测试 Spring Boot

```bash
# 启动 Spring Boot
./mvnw spring-boot:run

# 测试检测接口
curl -X POST http://localhost:8000/api/v1/detection/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test.jpg"
```

### 3. 前端联调

```javascript
// 在前端测试上传
const formData = new FormData();
formData.append('file', file);

const res = await request({
  url: '/detection/upload',
  method: 'post',
  data: formData,
  headers: {
    'Content-Type': 'multipart/form-data'
  }
});

console.log('检测结果', res.data);
```

---

## 🎯 集成清单

### YOLO 服务端（Python）
- [x] 安装依赖（ultralytics, fastapi）
- [x] 创建推理服务
- [x] 实现图片检测接口
- [x] 实现视频帧检测接口
- [x] 健康检查接口
- [x] 启动服务（端口 8001）

### Spring Boot 后端（Java）
- [x] 添加 HTTP 客户端依赖
- [x] 创建 YoloService
- [x] 创建 DetectionController
- [x] 配置文件上传
- [x] WebSocket 配置（实时检测）
- [x] 保存检测记录到数据库

### 前端（Vue）
- [x] 创建文件上传组件
- [x] WebSocket 连接
- [x] 显示检测结果
- [x] 绘制检测框

---

## 📝 代码示例：完整流程

### 前端上传图片

```vue
<template>
  <div>
    <input type="file" @change="handleFileChange" accept="image/*" />
    <button @click="handleDetect">开始检测</button>
    
    <!-- 显示结果 -->
    <div v-if="result">
      <img :src="result.annotated_image" />
      <div v-for="det in result.detections" :key="det">
        <p>{{ det.class_name }}: {{ (det.confidence * 100).toFixed(1) }}%</p>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  data() {
    return {
      selectedFile: null,
      result: null
    };
  },
  methods: {
    handleFileChange(e) {
      this.selectedFile = e.target.files[0];
    },
    
    async handleDetect() {
      if (!this.selectedFile) return;
      
      const formData = new FormData();
      formData.append('file', this.selectedFile);
      formData.append('serialNo', 'SN-TEST-001');
      
      try {
        const res = await request({
          url: '/detection/upload',
          method: 'post',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        this.result = res.data;
        console.log('检测完成', this.result);
      } catch (error) {
        console.error('检测失败', error);
      }
    }
  }
};
</script>
```

---

## 🎉 集成完成标志

### ✅ 所有功能正常工作

- [ ] YOLO 服务能独立运行
- [ ] Spring Boot 能调用 YOLO 服务
- [ ] 图片上传检测功能正常
- [ ] 检测结果能正确返回
- [ ] 前端能显示标注图片
- [ ] WebSocket 实时推送正常
- [ ] 检测记录能保存到数据库
- [ ] 性能满足要求（推理 < 100ms）

---

## 💡 最佳实践

### 1. 错误处理
- YOLO 服务异常时，Spring Boot 返回友好提示
- 添加重试机制（最多 3 次）
- 记录错误日志

### 2. 并发控制
- 使用队列控制并发请求
- 避免 YOLO 服务过载
- 限流保护

### 3. 模型热更新
- 支持不停机更新模型
- 模型版本管理
- A/B 测试支持

### 4. 监控告警
- 监控 YOLO 服务状态
- 监控推理耗时
- 异常自动告警

---

## 📞 常见问题

### Q1: YOLO 服务调用超时怎么办？
**A:** 增加超时时间，或优化模型推理速度

### Q2: 如何支持多个 YOLO 模型？
**A:** 在 Python 服务中加载多个模型，通过参数选择

### Q3: 如何处理大批量图片？
**A:** 使用消息队列（RabbitMQ/Kafka）异步处理

### Q4: GPU 服务器如何部署？
**A:** 使用 Docker + NVIDIA Container Toolkit

### Q5: 如何提升检测速度？
**A:** 使用 TensorRT 优化、批量推理、模型量化

---

## 🚀 开始集成

**按照以下步骤：**

1. ✅ 搭建 YOLO 推理服务（1天）
2. ✅ Spring Boot 调用测试（1天）
3. ✅ 集成到检测流程（2天）
4. ✅ 前端联调测试（1天）
5. ✅ 性能优化（1-2天）

**预计集成时间：** 1 周

**祝集成顺利！** 🎊
