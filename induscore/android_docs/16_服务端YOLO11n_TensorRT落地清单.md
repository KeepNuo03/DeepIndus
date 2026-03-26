# 服务端 YOLO11n + TensorRT 落地清单

## 1. 目标与边界
- 目标：降低推理时延、提升吞吐、降低单次推理成本。
- 场景：仅适用于服务端 NVIDIA GPU（CUDA）环境。
- 边界：安卓端本地部署不使用 TensorRT（安卓端优先 TFLite/NCNN）。

## 2. 验收指标（先定义）
- 时延：P95 至少下降 20%~30%（相对当前基线）。
- 吞吐：QPS 至少提升 1.3x~2x（视硬件和 batch 而定）。
- 精度：mAP 回退可控（建议 <= 1%）。
- 稳定性：连续压测 30~60 分钟无异常崩溃与 OOM。

## 3. 环境冻结（必须）
- 硬件：GPU 型号、显存、驱动版本。
- 软件：CUDA、TensorRT、cuDNN、Python、推理服务框架版本。
- 模型：YOLO11n 权重版本、输入尺寸、阈值配置。
- 输出：`env.md`（版本锁定文档），作为可复现基线。

## 4. 基线测试（优化前）
- 测当前线上方案（PyTorch/ONNXRuntime）：
  - P50/P95/P99
  - QPS
  - GPU 利用率与显存占用
  - 错误率
- 输出：`baseline_report.md`，后续和 TensorRT 对照。

## 5. 模型导出链路
1. 导出 ONNX（固定 opset）。
2. 可选：ONNX Simplifier 简化图结构。
3. ONNXRuntime 冒烟验证（同一批图片确认结果合理）。

## 6. TensorRT 构建策略
- 第一阶段：FP16 engine（优先，收益/风险比最好）。
- 第二阶段：INT8 engine（需要校准集，追求极致吞吐）。
- 形状策略：
  - 固定输入尺寸优先（最稳定）
  - 动态 shape 仅在确有需求时启用并设置 min/opt/max profile

## 7. 服务接入方式
- 方案 A：应用内直接调用 TensorRT runtime。
- 方案 B：Triton Inference Server（推荐大规模生产）。
- 必做：
  - 保持上层业务 API 不变
  - 增加后端开关：`backend=onnx|tensorrt`
  - 支持灰度与一键回滚

## 8. 精度一致性验证
- 同一批测试集对比 ONNX/TensorRT：
  - 检测框数量差异
  - 类别一致性
  - 置信度分布
  - 漏检/误检样本
- 输出：`accuracy_compare.md`（包含样例截图编号）。

## 9. 性能压测与容量评估
- 压测维度：
  - batch=1（低时延）
  - batch>1（高吞吐）
  - 并发压力递增（例如 1/5/10/20）
- 采集指标：
  - QPS、P95、GPU/显存、失败率
- 输出：`perf_report.md` + 容量建议（单机可承载并发）。

## 10. 上线与回滚
- 灰度步骤：5% -> 20% -> 50% -> 100%。
- 监控告警：
  - P95 突增
  - 错误率上升
  - GPU OOM
- 回滚预案：
  - 开关回退到 ONNX/PyTorch
  - 保留旧镜像和旧引擎文件

## 11. 迭代优化建议
- 先 FP16 稳定运行，再评估 INT8。
- 按业务流量特征优化 batch 与队列策略。
- 对热门机型/分辨率构建多套 engine 做路由。

---

## 附：FP32 / FP16 / INT8 是什么

你前面提到的 `IN18`，通常是 `INT8` 的笔误。

### FP32（32位浮点）
- 精度最高，体积和算力开销最大。
- 常用于训练和推理基线对照。

### FP16（16位浮点）
- 精度略低于 FP32，但通常损失很小。
- 速度更快、显存占用更低。
- 在 TensorRT 中通常是第一优先优化模式。

### INT8（8位整数量化）
- 体积最小、速度通常最快。
- 需要校准数据（calibration），否则精度波动可能较大。
- 适合在 FP16 稳定后进行第二阶段优化。

## 推荐实践
- 生产落地顺序：`FP32基线 -> FP16上线 -> INT8优化`。

## 执行进度（2026-03-26）

- 已完成服务端推理加速第一阶段代码改造（MVP）：
  - `yolo-service/main.py` 新增后端切换：`YOLO_BACKEND=auto|pytorch|onnx|tensorrt`
  - `auto` 策略优先级：`TensorRT engine -> ONNX -> PyTorch`
  - 新增 FP16 开关、预热（warmup）、回退开关与推理参数配置
  - 保持原有检测 API 路径不变（兼容现有上层业务调用）
- 已补齐运行材料：
  - `yolo-service/.env.example`
  - `yolo-service/requirements.txt`
  - `yolo-service/README.md`
- 已完成语法级验证：
  - `python -m py_compile yolo-service/main.py` 通过
