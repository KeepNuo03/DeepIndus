-- DeepIndus demo seed data (anonymized)
USE induscore;

-- Minimal agent demo session
INSERT INTO agent_session (session_id, user_id, client_type, title, status)
VALUES ('demo_session_001', 1, 'pc', '今日告警趋势', 'active')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO agent_message (session_id, message_role, content, token_count, trace_id)
VALUES
  ('demo_session_001', 'user', '帮我分析今日告警趋势', 12, 'demo_trace_001'),
  ('demo_session_001', 'assistant', '结论：告警总体较低，但仍存在1条严重告警。依据：示例数据。下一步：优先复核严重告警。', 42, 'demo_trace_001');

INSERT INTO agent_tool_call_log (session_id, trace_id, tool_name, input_json, output_json, success, error_code, latency_ms)
VALUES
  ('demo_session_001', 'demo_trace_001', 'getDashboardAlerts', '{"limit":10}', '{"critical":1}', 1, NULL, 27);

INSERT INTO agent_audit_log (trace_id, session_id, user_id, client_type, request_summary, response_status, model_name, model_latency_ms, prompt_tokens, completion_tokens, total_tokens)
VALUES
  ('demo_trace_001', 'demo_session_001', 1, 'pc', '分析今日告警趋势', 'ok', 'demo-model', 320, 120, 90, 210)
ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP;
