/**
 * Agent 聊天与会话管理 API
 */
import { get, post, del, BASE_URL } from '@/utils/request';

function buildAuthHeaders(extraHeaders = {}) {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    'X-Client-Type': 'pc',
    ...extraHeaders,
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

export function buildAgentContext(scene = 'web_general', pageParams = {}) {
  return {
    scene,
    pageParams,
  };
}

/**
 * 同步问答
 */
export function chatAgent(data) {
  return post('/agent/chat', data);
}

/**
 * 会话列表
 */
export function getAgentSessions(params = {}) {
  return get('/agent/sessions', params);
}

/**
 * 会话详情
 */
export function getAgentSessionDetail(sessionId) {
  const encodedId = encodeURIComponent(String(sessionId));
  return get(`/agent/sessions/${encodedId}`);
}

/**
 * 归档会话
 */
export function archiveAgentSession(sessionId) {
  const encodedId = encodeURIComponent(String(sessionId));
  return del(`/agent/sessions/${encodedId}`);
}

/**
 * 流式问答（POST + SSE）
 * handlers:
 * - onOpen(meta)
 * - onTool(toolEvent)
 * - onChunk(chunkText)
 * - onDone(doneData)
 * - onError(errorData)
 */
export async function streamAgentChat(data, handlers = {}, options = {}) {
  const {
    onOpen,
    onTool,
    onChunk,
    onDone,
    onError,
  } = handlers;
  const { signal, retry = 1 } = options;

  let attempt = 0;
  let delayMs = 300;
  let lastError = null;

  while (attempt <= retry) {
    try {
      await streamOnce(data, {
        onOpen,
        onTool,
        onChunk,
        onDone,
        onError,
        signal,
      });
      return;
    } catch (error) {
      if (signal?.aborted) {
        throw new Error('请求已取消');
      }
      lastError = error;
      if (attempt >= retry) break;
      await new Promise((resolve) => setTimeout(resolve, delayMs));
      delayMs *= 2;
      attempt += 1;
    }
  }

  throw lastError || new Error('流式请求失败');
}

async function streamOnce(data, handlers) {
  const response = await fetch(`${BASE_URL}/agent/chat/stream`, {
    method: 'POST',
    headers: buildAuthHeaders(),
    body: JSON.stringify(data),
    signal: handlers.signal,
  });

  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
    throw new Error('登录已过期，请重新登录');
  }

  if (!response.ok) {
    let message = '流式请求失败';
    try {
      const err = await response.json();
      const code = err?.data?.errorCode || err?.errorCode || err?.code;
      const traceId = err?.data?.traceId || err?.traceId;
      message = [err?.message || message, code ? `(${code})` : '', traceId ? `traceId: ${traceId}` : '']
        .filter(Boolean)
        .join(' ');
    } catch (_) {
      // ignore parse error
    }
    throw new Error(message);
  }

  if (!response.body) {
    throw new Error('流式响应为空');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let buffer = '';

  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const blocks = buffer.split('\n\n');
    buffer = blocks.pop() || '';
    for (const block of blocks) {
      handleSseBlock(block, handlers);
    }
  }

  if (buffer.trim()) {
    handleSseBlock(buffer, handlers);
  }
}

function handleSseBlock(block, handlers) {
  const lines = block
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);
  if (!lines.length) return;

  let eventName = 'message';
  let dataLine = '';

  lines.forEach((line) => {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim();
    } else if (line.startsWith('data:')) {
      dataLine += line.slice(5).trim();
    }
  });

  let payload = {};
  if (dataLine) {
    try {
      payload = JSON.parse(dataLine);
    } catch {
      payload = { text: dataLine };
    }
  }

  if (eventName === 'meta') {
    handlers.onOpen?.(payload);
    return;
  }

  if (eventName === 'tool') {
    handlers.onTool?.(payload);
    return;
  }

  if (eventName === 'chunk') {
    handlers.onChunk?.(payload.text || '');
    return;
  }

  if (eventName === 'done') {
    handlers.onDone?.(payload);
    return;
  }

  if (eventName === 'error') {
    handlers.onError?.(payload);
    const message = payload.message || '流式返回错误';
    const code = payload.code ? `(${payload.code})` : '';
    const traceId = payload.traceId ? `traceId: ${payload.traceId}` : '';
    throw new Error([message, code, traceId].filter(Boolean).join(' '));
  }
}
