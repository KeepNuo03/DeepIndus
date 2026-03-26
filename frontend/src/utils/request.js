/**
 * 统一请求工具 - 封装 fetch API
 * 自动添加 token，统一错误处理
 */

export const BASE_URL = 'http://localhost:8000/v1';

/**
 * 统一请求方法
 * @param {string} url - 接口路径
 * @param {object} options - fetch 配置项
 * @returns {Promise} - 返回 data 部分
 */
async function request(url, options = {}) {
  // 完整 URL
  const fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`;

  // 默认配置
  const defaultOptions = {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // 合并配置
  const config = { ...defaultOptions, ...options };

  // 添加 Token（登录和注册接口不需要）
  const token = localStorage.getItem('token');
  if (token && !url.includes('/auth/')) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  try {
    const response = await fetch(fullUrl, config);
    let data;
    try {
      data = await response.json();
    } catch (parseErr) {
      console.error('响应非 JSON', { url: fullUrl, status: response.status }, parseErr);
      throw new Error(response.status === 500 ? '服务器返回异常，请查看控制台或联系后端' : '响应格式错误');
    }

    // 处理业务状态码
    if (data.code === 200) {
      return data;
    } else if (data.code === 401) {
      // Token 过期或无效
      alert('登录已过期，请重新登录');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
      throw new Error('Unauthorized');
    } else {
      // 其他业务错误，开发环境打印便于排查
      if (process.env.NODE_ENV !== 'production') {
        console.error('接口业务错误', { url: fullUrl, code: data.code, message: data.message, data });
      }
      throw new Error(data.message || '请求失败');
    }
  } catch (error) {
    // 网络错误或解析错误
    if (error.message === 'Unauthorized') {
      throw error;
    }
    console.error('请求错误：', { url: fullUrl, error });
    throw error;
  }
}

/**
 * GET 请求
 */
export function get(url, params = {}) {
  // 构建查询字符串
  const queryString = new URLSearchParams(params).toString();
  const fullUrl = queryString ? `${url}?${queryString}` : url;

  return request(fullUrl, {
    method: 'GET',
  });
}

/**
 * POST 请求
 */
export function post(url, data = {}) {
  return request(url, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * PUT 请求
 */
export function put(url, data = {}) {
  return request(url, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

/**
 * DELETE 请求
 */
export function del(url, data = {}) {
  return request(url, {
    method: 'DELETE',
    body: JSON.stringify(data),
  });
}

/**
 * 文件上传（multipart/form-data）
 */
export function upload(url, formData) {
  const token = localStorage.getItem('token');

  return fetch(`${BASE_URL}${url}`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData, // 不设置 Content-Type，让浏览器自动设置
  }).then((res) => res.json());
}

export default request;
