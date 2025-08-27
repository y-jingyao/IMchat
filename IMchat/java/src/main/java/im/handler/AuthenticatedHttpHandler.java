package im.handler;

import im.service.JwtService;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// 需要JWT认证的Handler基类
public abstract class AuthenticatedHttpHandler extends BaseHttpHandler {
    private final JwtService jwtService = new JwtService();

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token.");
            return;
        }

        String token = authHeader.substring(7);
        Long userId = jwtService.getUserIdFromToken(token);

        if (userId == null) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token.");
            return;
        }

        handleAuthenticatedRequest(exchange, userId);
    }

    // 让子类根据请求方法决定如何处理
    protected void handleAuthenticatedRequest(HttpExchange exchange, Long userId) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> requestBody = gson.fromJson(isr, type);
            handleAuthenticatedPostRequest(exchange, requestBody, userId);
        } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            //
            //
            //
        } else {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
        }
    }

    protected void handleAuthenticatedPostRequest(HttpExchange exchange, Map<String, Object> requestBody, Long userId) throws IOException {
        sendErrorResponse(exchange, 405, "Method Not Allowed");
    }
}