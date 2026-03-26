# YOLO 安卓部署方案

## 目标
在不影响主链路稳定性的前提下，将安卓本地推理用于“现场快速预判”。

## 建议定位
- 主检测链路仍在服务端（Spring Boot -> YOLO 服务）。
- 安卓本地 YOLO 为可选辅助能力，不作为最终裁决依据。

## 技术路线
优先级建议：
1. `TFLite`（首选，资料多、接入快）
2. `NCNN`（性能更优，但集成更复杂）

## 标准实施步骤
1. 训练端导出模型：YOLO -> ONNX/TFLite。
2. 量化压缩：FP16 或 INT8。
3. 安卓端接入推理引擎。
4. 实现前处理（缩放、归一化）与后处理（NMS）。
5. 真机压测（时延、发热、耗电、精度）。
6. 增加开关：可关闭本地推理，回退到纯云端检测。

## 当前已落地（2026-03，FP16 TFLite 接入）
- 已完成本地推理真实实现（不影响上传主链路）：
  - `LocalYoloEngine` 接口
  - `LocalInferenceResult` / `LocalDetection` 统一结果结构
  - `TfliteLocalYoloEngine`（模型加载、图片预处理、推理执行、NMS 后处理）
  - `NoOpLocalYoloEngine` 作为降级兜底（初始化失败/手动关闭时启用）
- 已完成配置项：
  - `LOCAL_YOLO_ENABLED`
  - `LOCAL_YOLO_MODEL_ASSET`
  - `LOCAL_YOLO_LABELS_ASSET`
  - 输入尺寸、阈值、最大结果数
- 已完成依赖接入：
  - `org.tensorflow:tensorflow-lite`
- 资产文件约定：
  - `app/src/main/assets/models/yolo11n_induscore.tflite`
  - `app/src/main/assets/models/labels.txt`
- 抽检上传页已具备“本地快速预判（YOLO PoC）”入口，可在入队前执行本地推理。
- 抽检上传页已支持“检测图片预览 + 检测框叠加渲染”（归一化坐标映射到 `ContentScale.Fit` 画布）。

## 下一步接入清单（稳定性与效果收口）
1. 将本地预判结果与上传记录关联（用于复核前参考，不覆盖服务端最终结论）。
2. 增加模型元信息校验（输入尺寸、类别数、标签文件一致性）与异常告警。
3. 执行 2 机型以上真机压测并回填指标（时延、温升、耗电、崩溃率）。
4. 根据试点结果调优置信度/NMS 阈值，沉淀机型分层参数建议。

## 建议性能目标
- 单帧推理时延：中端机 < 300ms（目标）
- 长时间运行温度可控（连续使用 15 分钟）
- 包体控制：模型建议支持按需下载

## 风险
- 机型碎片化导致推理耗时波动大。
- 本地结果与服务端结果差异会造成业务争议。

## 下一步
- 模型侧提供首版 `yolo11n` 移动模型（建议 FP16），进入真实 TFLite 推理接入阶段。
