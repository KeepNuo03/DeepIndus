package com.induscore.service.agent.model;

import com.induscore.dto.agent.AgentDtos;
import com.induscore.security.RequestAuthContext;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/**
 * 基于 LangChain4j 的模型网关。
 * 当未配置 API Key 或关闭开关时自动降级为稳定占位回复，避免阻塞联调。
 */
@Component
public class LangChain4jModelGateway implements ModelGateway {
    private static final Logger log = LoggerFactory.getLogger(LangChain4jModelGateway.class);

    private final boolean enabled;
    private final boolean fallbackEnabled;
    private final String provider;
    private final String primaryModelName;
    private final String fallbackModelName;
    private final boolean retryEnabled;
    private final int retryMaxAttempts;
    private final long retryInitialBackoffMs;
    private final long retryMaxBackoffMs;
    private final OpenAiChatModel primaryModel;
    private final OpenAiChatModel fallbackModel;

    public LangChain4jModelGateway(
            @Value("${agent.model.enabled:false}") boolean enabled,
            @Value("${agent.model.provider:openai}") String provider,
            @Value("${agent.model.api-key:}") String apiKey,
            @Value("${agent.model.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${agent.model.name:gpt-4o-mini}") String modelName,
            @Value("${agent.model.temperature:0.2}") double temperature,
            @Value("${agent.model.timeout-ms:12000}") long timeoutMs,
            @Value("${agent.model.retry.enabled:true}") boolean retryEnabled,
            @Value("${agent.model.retry.max-attempts:3}") int retryMaxAttempts,
            @Value("${agent.model.retry.initial-backoff-ms:800}") long retryInitialBackoffMs,
            @Value("${agent.model.retry.max-backoff-ms:5000}") long retryMaxBackoffMs,
            @Value("${agent.model.fallback.enabled:false}") boolean fallbackEnabled,
            @Value("${agent.model.fallback.api-key:}") String fallbackApiKey,
            @Value("${agent.model.fallback.base-url:}") String fallbackBaseUrl,
            @Value("${agent.model.fallback.name:}") String fallbackModelName
    ) {
        this.provider = provider == null || provider.isBlank() ? "openai" : provider.trim().toLowerCase();
        this.enabled = enabled && apiKey != null && !apiKey.isBlank();
        this.primaryModelName = modelName;
        this.retryEnabled = retryEnabled;
        this.retryMaxAttempts = Math.max(1, retryMaxAttempts);
        this.retryInitialBackoffMs = Math.max(200L, retryInitialBackoffMs);
        this.retryMaxBackoffMs = Math.max(this.retryInitialBackoffMs, retryMaxBackoffMs);

        if (this.enabled) {
            this.primaryModel = OpenAiChatModel.builder()
                    .apiKey(apiKey.trim())
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .temperature(temperature)
                    .timeout(Duration.ofMillis(Math.max(timeoutMs, 3_000)))
                    .build();
        } else {
            this.primaryModel = null;
        }

        boolean hasFallbackApiKey = fallbackApiKey != null && !fallbackApiKey.isBlank();
        boolean hasFallbackModelName = fallbackModelName != null && !fallbackModelName.isBlank();
        this.fallbackEnabled = this.enabled && fallbackEnabled && hasFallbackApiKey && hasFallbackModelName;
        this.fallbackModelName = hasFallbackModelName ? fallbackModelName.trim() : null;
        if (this.fallbackEnabled) {
            String actualFallbackBaseUrl = (fallbackBaseUrl == null || fallbackBaseUrl.isBlank()) ? baseUrl : fallbackBaseUrl;
            this.fallbackModel = OpenAiChatModel.builder()
                    .apiKey(fallbackApiKey.trim())
                    .baseUrl(actualFallbackBaseUrl)
                    .modelName(this.fallbackModelName)
                    .temperature(temperature)
                    .timeout(Duration.ofMillis(Math.max(timeoutMs, 3_000)))
                    .build();
        } else {
            this.fallbackModel = null;
        }

        log.info(
                "agent_model_status enabled={} provider={} primaryModel={} baseUrl={} apiKeyConfigured={} timeoutMs={} retryEnabled={} retryMaxAttempts={} fallbackEnabled={} fallbackModel={}",
                this.enabled,
                this.provider,
                this.primaryModelName,
                baseUrl,
                apiKey != null && !apiKey.isBlank(),
                Math.max(timeoutMs, 3_000),
                this.retryEnabled,
                this.retryMaxAttempts,
                this.fallbackEnabled,
                this.fallbackModelName
        );
    }

    @Override
    public String generateReply(RequestAuthContext context, AgentDtos.ChatRequest request, String prompt) {
        if (!enabled || primaryModel == null) {
            return "当前处于 Agent 联调模式（未启用真实模型）。已完成工具数据聚合，请先配置 AGENT_MODEL_ENABLED=true 与 AGENT_MODEL_API_KEY 后启用真实推理。";
        }
        Exception primaryFailure = null;
        try {
            return generateWithRetry(primaryModel, primaryModelName, prompt);
        } catch (Exception ex) {
            primaryFailure = ex;
            log.warn("agent_primary_model_failed model={} message={}", primaryModelName, safeError(ex));
        }

        if (fallbackEnabled && fallbackModel != null) {
            try {
                return generateWithRetry(fallbackModel, fallbackModelName, prompt);
            } catch (Exception fallbackEx) {
                log.warn("agent_fallback_model_failed model={} message={}", fallbackModelName, safeError(fallbackEx));
                throw fallbackEx;
            }
        }
        if (primaryFailure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new RuntimeException(primaryFailure);
    }

    private String generateWithRetry(OpenAiChatModel model, String modelName, String prompt) {
        int attempts = retryEnabled ? retryMaxAttempts : 1;
        Exception lastError = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                String response = model.generate(prompt); //LangChain4j生成回复
                if (response == null || response.isBlank()) {
                    return "模型未返回有效内容，请稍后重试。";
                }
                return response.trim();
            } catch (Exception ex) {
                lastError = ex;
                boolean retryable = isRetryable(ex);
                if (!retryable || i >= attempts) {
                    throw new RuntimeException(ex);
                }
                long backoff = calculateBackoffMs(i);
                log.warn("agent_model_retrying model={} attempt={}/{} backoffMs={} reason={}",
                        modelName, i, attempts, backoff, safeError(ex));
                sleepQuietly(backoff);
            }
        }
        throw new RuntimeException(lastError);
    }

    private boolean isRetryable(Exception ex) {
        String raw = safeError(ex).toLowerCase(Locale.ROOT);
        return raw.contains("engine_overloaded_error")
                || raw.contains("overloaded")
                || raw.contains("timeout")
                || raw.contains("timed out")
                || raw.contains("429")
                || raw.contains("503");
    }

    private long calculateBackoffMs(int attempt) {
        long value = retryInitialBackoffMs * (1L << Math.max(0, attempt - 1));
        return Math.min(value, retryMaxBackoffMs);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private String safeError(Throwable ex) {
        return ex == null || ex.getMessage() == null ? "unknown" : ex.getMessage();
    }

    public String modelName() {
        return primaryModelName;
    }

    public String fallbackModelName() {
        return fallbackModelName;
    }

    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    public boolean isRetryEnabled() {
        return retryEnabled;
    }

    public int retryMaxAttempts() {
        return retryMaxAttempts;
    }
}
