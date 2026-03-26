package com.induscore.service.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.induscore.common.ApiException;
import com.induscore.dto.agent.AgentDtos;
import com.induscore.security.RequestAuthContext;
import com.induscore.service.agent.model.ModelGateway;
import com.induscore.service.agent.tool.AgentToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * LangChain4j 编排实现（当前阶段）：
 * - 根据 scene 与问题意图选择业务工具；
 * - 将工具数据拼接为结构化上下文，交给模型生成回答；
 * - 优先保证“问题 -> 对应业务数据”的命中率。
 */
@Component
public class DefaultAgentOrchestrator implements AgentOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(DefaultAgentOrchestrator.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ModelGateway modelGateway;
    private final AgentToolService toolService;

    public DefaultAgentOrchestrator(ModelGateway modelGateway, AgentToolService toolService) {
        this.modelGateway = modelGateway;
        this.toolService = toolService;
    }

    @Override
    public AgentDraft run(RequestAuthContext context, AgentDtos.ChatRequest request) {
        // scene 决定默认工具策略；web_* 允许走更宽松的自动补数逻辑。
        String scene = request.context() == null || request.context().scene() == null
                ? "general"
                : request.context().scene().toLowerCase(Locale.ROOT);
        String userInput = request.messages().get(request.messages().size() - 1).content();

        List<AgentDtos.ToolCall> toolCalls = new ArrayList<>();
        Map<String, Object> toolData = new LinkedHashMap<>();
        switch (scene) {
            case "workbench" -> {
                timedToolCall(toolCalls, "getMobileWorkbench", () -> toolData.put("workbench", toolService.getMobileWorkbench()));
                timedToolCall(toolCalls, "getMobileNotifications", () -> toolData.put("notifications", toolService.getMobileNotifications(5)));
            }
            case "upload_status" -> {
                String idempotencyKey = request.context() == null ? null : request.context().idempotencyKey();
                if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                    timedToolCall(toolCalls, "getMobileUploadStatus", () -> toolData.put("uploadStatus", toolService.getMobileUploadStatus(idempotencyKey)));
                }
            }
            case "review_list", "review_detail", "detection_records" -> attachDetectionTools(request, toolCalls, toolData);
            case "metrics", "dashboard", "web_dashboard" -> attachDashboardTools(request, toolCalls, toolData);
            case "production_line", "line_overview" -> attachProductionLineTools(request, toolCalls, toolData);
            case "model_mgmt", "model_management" -> attachModelManagementTools(toolCalls, toolData);
            default -> {
                // general 场景不强制工具，减少不必要调用成本
            }
        }
        // 对“数据型问题”做兜底补数，降低模型泛化回答概率。
        if (shouldAttachWorkbenchData(scene, userInput, toolData)) {
            timedToolCall(toolCalls, "getMobileWorkbench", () -> toolData.put("workbench", toolService.getMobileWorkbench()));
        }
        if (shouldAttachDetectionData(scene, userInput, toolData)) {
            attachDetectionTools(request, toolCalls, toolData);
        }
        if (shouldAttachDashboardData(scene, userInput, toolData)) {
            attachDashboardTools(request, toolCalls, toolData);
        }
        if (shouldAttachProductionLineData(scene, userInput, toolData)) {
            attachProductionLineTools(request, toolCalls, toolData);
        }
        if (shouldAttachModelManagementData(scene, userInput, toolData)) {
            attachModelManagementTools(toolCalls, toolData);
        }

        String prompt = buildPrompt(context, request, scene, userInput, toolData); //组建提示词
        String answer;
        try {
            answer = modelGateway.generateReply(context, request, prompt);
        } catch (Exception ex) {
            log.error("agent_model_call_failed scene={} userId={}", scene, context.userId(), ex);
            String rawError = collectErrorText(ex).toLowerCase(Locale.ROOT);
            if (rawError.contains("engine_overloaded_error") || rawError.contains("overloaded")) {
                throw new ApiException(529, "模型服务当前繁忙，请稍后重试");
            }
            if (rawError.contains("exceeded_current_quota_error") || rawError.contains("insufficient balance")) {
                throw new ApiException(402, "模型账户额度不足，请检查计费与余额");
            }
            if (rawError.contains("timeout")) {
                throw new ApiException(504, "模型响应超时，请稍后重试");
            }
            throw new ApiException(503, "模型调用失败，请检查模型配置、网络连通性或模型名称");
        }

        int promptTokens = estimateTokens(userInput);
        int completionTokens = estimateTokens(answer);
        return new AgentDraft(
                answer,
                toolCalls,
                new AgentDtos.Usage(promptTokens, completionTokens, promptTokens + completionTokens)
        );
    }

    private String buildPrompt(
            RequestAuthContext context,
            AgentDtos.ChatRequest request,
            String scene,
            String userInput,
            Map<String, Object> toolData
    ) {
        // 一期仍是“模板 + 工具数据拼接”方案，先保证稳定性和可控性。
        String conversationHistory = buildConversationHistory(request.messages());
        StringBuilder sb = new StringBuilder();
        sb.append("你是DeepIndus工业质检平台的业务助手。")
                .append("请基于已提供业务数据回答，不要编造。")
                .append("若数据不足，明确说明缺失项并给下一步建议。\n\n")
                .append("用户上下文:\n")
                .append("- userId: ").append(context.userId()).append('\n')
                .append("- clientType: ").append(context.clientType()).append('\n')
                .append("- scene: ").append(scene).append('\n')
                .append("- page: ").append(request.context() == null ? "unknown" : request.context().page()).append('\n')
                .append("- taskId: ").append(request.context() == null ? null : request.context().taskId()).append('\n')
                .append("- idempotencyKey: ").append(request.context() == null ? null : request.context().idempotencyKey()).append('\n')
                .append('\n')
                .append("近期对话上下文:\n")
                .append(conversationHistory)
                .append("\n\n")
                .append("工具数据:\n")
                .append(toJson(toolData))
                .append("\n\n")
                .append("用户问题:\n")
                .append(userInput)
                .append("\n\n")
                .append("输出要求:\n")
                .append("1) 先给结论，再给依据；2) 给出不超过3条可执行下一步；3) 用中文简明表达。");
        return sb.toString();
    }

    /**
     * 将最近窗口对话压缩为可读文本，控制单轮 Prompt 体积。
     */
    private String buildConversationHistory(List<AgentDtos.ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        int keep = Math.min(messages.size(), 12);
        int from = Math.max(0, messages.size() - keep);
        for (int i = from; i < messages.size(); i++) {
            AgentDtos.ChatMessage message = messages.get(i);
            String role = message.role() == null ? "unknown" : message.role().toLowerCase(Locale.ROOT);
            String content = message.content() == null ? "" : message.content();
            if (content.length() > 320) {
                content = content.substring(0, 320) + "...";
            }
            sb.append("- ").append(role).append(": ").append(content).append('\n');
        }
        return sb.toString();
    }

    private void timedToolCall(List<AgentDtos.ToolCall> toolCalls, String toolName, Runnable runnable) {
        long startedAt = System.currentTimeMillis();
        try {
            runnable.run();
            toolCalls.add(new AgentDtos.ToolCall(toolName, true, System.currentTimeMillis() - startedAt));
        } catch (ApiException ex) {
            toolCalls.add(new AgentDtos.ToolCall(toolName, false, System.currentTimeMillis() - startedAt));
            throw ex;
        } catch (Exception ex) {
            toolCalls.add(new AgentDtos.ToolCall(toolName, false, System.currentTimeMillis() - startedAt));
            throw new ApiException(503, "工具调用失败: " + toolName);
        }
    }

    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 1;
        }
        return Math.max(1, text.length() / 2);
    }

    private boolean shouldAttachWorkbenchData(String scene, String userInput, Map<String, Object> toolData) {
        // 已经有 workbench 数据时不重复调用，避免多余耗时。
        if (toolData.containsKey("workbench")) {
            return false;
        }
        if (scene != null && scene.startsWith("web_")) {
            return true;
        }
        if (userInput == null || userInput.isBlank()) {
            return false;
        }
        String q = userInput.toLowerCase(Locale.ROOT);
        return q.contains("待复核")
                || q.contains("处理中")
                || q.contains("抽检")
                || q.contains("告警")
                || q.contains("pendingreviewcount")
                || q.contains("processingcount")
                || q.contains("todaysamplingcount")
                || q.contains("severealertcount");
    }

    private boolean shouldAttachDetectionData(String scene, String userInput, Map<String, Object> toolData) {
        if (toolData.containsKey("detectionRecordsOverview")) {
            return false;
        }
        if ("detection_records".equals(scene) || "review_list".equals(scene) || "review_detail".equals(scene)) {
            return true;
        }
        if (userInput == null || userInput.isBlank()) {
            return false;
        }
        String q = userInput.toLowerCase(Locale.ROOT);
        return q.contains("检测记录")
                || q.contains("复核任务")
                || q.contains("缺陷分布")
                || q.contains("缺陷趋势")
                || q.contains("review")
                || q.contains("defect");
    }

    private boolean shouldAttachDashboardData(String scene, String userInput, Map<String, Object> toolData) {
        if (toolData.containsKey("dashboardKpi")) {
            return false;
        }
        if ("dashboard".equals(scene) || "web_dashboard".equals(scene) || "metrics".equals(scene)) {
            return true;
        }
        if (userInput == null || userInput.isBlank()) {
            return false;
        }
        String q = userInput.toLowerCase(Locale.ROOT);
        return q.contains("数据大屏")
                || q.contains("kpi")
                || q.contains("良率")
                || q.contains("稼动率")
                || q.contains("告警趋势")
                || q.contains("dashboard")
                || q.contains("trend");
    }

    private boolean shouldAttachProductionLineData(String scene, String userInput, Map<String, Object> toolData) {
        if (toolData.containsKey("productionLinesOverview")) {
            return false;
        }
        if ("production_line".equals(scene) || "line_overview".equals(scene)) {
            return true;
        }
        if (userInput == null || userInput.isBlank()) {
            return false;
        }
        String q = userInput.toLowerCase(Locale.ROOT);
        return q.contains("生产线")
                || q.contains("产线")
                || q.contains("line")
                || q.contains("节拍")
                || q.contains("稼动率")
                || q.contains("停线");
    }

    private boolean shouldAttachModelManagementData(String scene, String userInput, Map<String, Object> toolData) {
        if (toolData.containsKey("modelManagementOverview")) {
            return false;
        }
        if ("model_mgmt".equals(scene) || "model_management".equals(scene)) {
            return true;
        }
        if (userInput == null || userInput.isBlank()) {
            return false;
        }
        String q = userInput.toLowerCase(Locale.ROOT);
        return q.contains("模型管理")
                || q.contains("模型版本")
                || q.contains("主模型")
                || q.contains("部署模型")
                || q.contains("model")
                || q.contains("version");
    }

    private void attachDetectionTools(AgentDtos.ChatRequest request, List<AgentDtos.ToolCall> toolCalls, Map<String, Object> toolData) {
        int page = getIntPageParam(request, "page", 1);
        int pageSize = getIntPageParam(request, "pageSize", 20);
        String status = getStringPageParam(request, "status");
        Long recordId = getLongPageParam(request, "recordId");

        timedToolCall(toolCalls, "getDetectionRecordsOverview", () ->
                toolData.put("detectionRecordsOverview", toolService.getDetectionRecordsOverview(page, pageSize, status)));
        timedToolCall(toolCalls, "getPilotMetrics", () ->
                toolData.put("pilotMetrics", toolService.getPilotMetrics()));
        if (recordId != null) {
            timedToolCall(toolCalls, "getDetectionRecordDetail", () ->
                    toolData.put("detectionRecordDetail", toolService.getDetectionRecordDetail(recordId)));
        }
    }

    private void attachDashboardTools(AgentDtos.ChatRequest request, List<AgentDtos.ToolCall> toolCalls, Map<String, Object> toolData) {
        String startTime = getStringPageParam(request, "startTime");
        String endTime = getStringPageParam(request, "endTime");
        timedToolCall(toolCalls, "getDashboardKpi", () ->
                toolData.put("dashboardKpi", toolService.getDashboardKpi()));
        timedToolCall(toolCalls, "getDashboardDefectTrend", () ->
                toolData.put("dashboardDefectTrend", toolService.getDashboardDefectTrend(startTime, endTime)));
        timedToolCall(toolCalls, "getDashboardDefectDistribution", () ->
                toolData.put("dashboardDefectDistribution", toolService.getDashboardDefectDistribution()));
        timedToolCall(toolCalls, "getDashboardAlerts", () ->
                toolData.put("dashboardAlerts", toolService.getDashboardAlerts()));
    }

    /**
     * 生产线场景工具挂载。
     */
    private void attachProductionLineTools(AgentDtos.ChatRequest request, List<AgentDtos.ToolCall> toolCalls, Map<String, Object> toolData) {
        Long lineId = getLongPageParam(request, "lineId");
        timedToolCall(toolCalls, "getProductionLinesOverview", () ->
                toolData.put("productionLinesOverview", toolService.getProductionLinesOverview()));
        if (lineId != null) {
            timedToolCall(toolCalls, "getProductionLineDetail", () ->
                    toolData.put("productionLineDetail", toolService.getProductionLineDetail(lineId)));
        }
    }

    /**
     * AI 模型管理场景工具挂载。
     */
    private void attachModelManagementTools(List<AgentDtos.ToolCall> toolCalls, Map<String, Object> toolData) {
        timedToolCall(toolCalls, "getModelManagementOverview", () ->
                toolData.put("modelManagementOverview", toolService.getModelManagementOverview()));
    }

    private int getIntPageParam(AgentDtos.ChatRequest request, String key, int defaultValue) {
        Object raw = getPageParam(request, key);
        if (raw == null) {
            return defaultValue;
        }
        if (raw instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(raw.toString().trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private Long getLongPageParam(AgentDtos.ChatRequest request, String key) {
        Object raw = getPageParam(request, key);
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(raw.toString().trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String getStringPageParam(AgentDtos.ChatRequest request, String key) {
        Object raw = getPageParam(request, key);
        if (raw == null) {
            return null;
        }
        String text = raw.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private Object getPageParam(AgentDtos.ChatRequest request, String key) {
        if (request.context() == null || request.context().pageParams() == null || key == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : request.context().pageParams().entrySet()) {
            if (Objects.equals(entry.getKey(), key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String collectErrorText(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable cursor = throwable;
        while (cursor != null) {
            if (cursor.getMessage() != null) {
                builder.append(cursor.getMessage()).append('\n');
            }
            cursor = cursor.getCause();
        }
        return builder.toString();
    }

    /**
     * 工具数据使用稳定 JSON 序列化，避免 Map.toString 带来的不可控格式波动。
     */
    private String toJson(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }
}
