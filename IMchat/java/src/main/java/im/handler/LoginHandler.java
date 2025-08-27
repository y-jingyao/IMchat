package im.handler;

import im.model.User;
import im.service.JwtService;
import im.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;

public class LoginHandler extends BaseHttpHandler {
    private final UserService userService = new UserService();
    private final JwtService jwtService = new JwtService();

    @Override
    protected void handlePost(HttpExchange exchange, Map<String, String> requestBody) throws IOException {
        String username = requestBody.get("username");
        String password = requestBody.get("password");

        User user = userService.login(username, password);

        if (user != null) {
            String token = jwtService.generateToken(user.getUid(), user.getUsername());
            sendResponse(exchange, 200, Map.of("token", token, "uid", user.getUid(),
                    "username", user.getUsername(),"nickname", user.getNickname(),
                        "creat_at",user.getCreatedAt(),"status0",user.getStatus()));
        } else {
            sendErrorResponse(exchange, 401, "Invalid username or password.");
        }
    }
}