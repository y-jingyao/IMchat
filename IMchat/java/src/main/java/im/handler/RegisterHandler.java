package im.handler;

import im.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;

public class RegisterHandler extends BaseHttpHandler {
    private final UserService userService = new UserService();

    @Override
    protected void handlePost(HttpExchange exchange, Map<String, String> requestBody) throws IOException {
        String username = requestBody.get("username");
        String password = requestBody.get("password");
        String nickname = requestBody.get("nickname");

        if (username == null || password == null || nickname == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            sendErrorResponse(exchange, 400, "请输入用户名和密码");
            return;
        }

        boolean success = userService.register(username, password, nickname);

        if (success) {
            sendResponse(exchange, 201, Map.of("message", "User registered successfully."));
        } else {
            sendErrorResponse(exchange, 409, "Username already exists.");
        }
    }
}