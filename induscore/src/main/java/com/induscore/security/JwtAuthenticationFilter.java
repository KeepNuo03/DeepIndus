package com.induscore.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.induscore.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * JWT 鉴权过滤器：对受保护接口进行 Token 校验。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 无需鉴权的接口路径（登录/注册、离线检测上传便于联调）。
     */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/v1/auth/login",
            "/v1/auth/register",
            "/auth/login",
            "/auth/register",
            "/v1/detection/upload"
    );

    private final JwtUtil jwtUtil;
    private final RequestAuthorizationService requestAuthorizationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtUtil jwtUtil, RequestAuthorizationService requestAuthorizationService) {
        this.jwtUtil = jwtUtil;
        this.requestAuthorizationService = requestAuthorizationService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // OPTIONS 预检请求不做鉴权
        if (PUBLIC_PATHS.contains(path) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 缺陷检测图像供详情页 <img src> 使用，浏览器不会带 Authorization
        if (path.startsWith("/v1/defect/") && path.endsWith("/image")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        // WebSocket 连接使用 query 参数携带 token
        if (path.startsWith("/ws")) {
            String token = request.getParameter("token");
            if (token == null || token.isBlank()) {
                writeUnauthorized(response, "未认证");
                return;
            }
            try {
                jwtUtil.parseToken(token);
                filterChain.doFilter(request, response);
                return;
            } catch (JwtException ex) {
                writeUnauthorized(response, "Token 无效或已过期");
                return;
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 未携带 Token
            writeUnauthorized(response, "未认证");
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        try {
            Claims claims = jwtUtil.parseToken(token);
            RequestAuthContext context = buildAuthContext(claims, request);
            RequestAuthContextHolder.set(context);
            request.setAttribute("authContext", context);
            AuthorizationDecision decision = requestAuthorizationService.authorize(request, context);
            if (!decision.allowed()) {
                writeForbidden(response, decision.message());
                return;
            }
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            writeUnauthorized(response, "Token 无效或已过期");
        } finally {
            RequestAuthContextHolder.clear();
        }
    }

    /**
     * 输出统一 401 响应。
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Object> body = ApiResponse.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Object> body = ApiResponse.error(403, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private RequestAuthContext buildAuthContext(Claims claims, HttpServletRequest request) {
        Long userId = parseLong(claims.get("userId"));
        String username = claims.getSubject();
        String clientType = request.getHeader("X-Client-Type");
        Set<String> roles = parseStringSet(claims.get("roles"));
        Set<String> permissions = parseStringSet(claims.get("permissions"));
        return new RequestAuthContext(userId, username, normalizeClientType(clientType), roles, permissions);
    }

    private Set<String> parseStringSet(Object rawValue) {
        if (rawValue instanceof Iterable<?> iterable) {
            Set<String> result = new HashSet<>();
            for (Object value : iterable) {
                if (value != null) {
                    String text = value.toString().trim().toLowerCase(Locale.ROOT);
                    if (!text.isBlank()) {
                        result.add(text);
                    }
                }
            }
            return result;
        }
        if (rawValue instanceof String text) {
            String normalized = text.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isBlank()) {
                return Set.of(normalized);
            }
        }
        if (rawValue instanceof Map<?, ?> map) {
            Set<String> result = new HashSet<>();
            for (Object value : map.values()) {
                if (value != null) {
                    String text = value.toString().trim().toLowerCase(Locale.ROOT);
                    if (!text.isBlank()) {
                        result.add(text);
                    }
                }
            }
            return result;
        }
        return Collections.emptySet();
    }

    private String normalizeClientType(String clientType) {
        if (clientType == null || clientType.isBlank()) {
            return "pc";
        }
        return clientType.trim().toLowerCase(Locale.ROOT);
    }

    private Long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
