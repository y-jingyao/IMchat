package im.handler.setting;

import im.handler.AuthenticatedHttpHandler;
import im.service.SettingService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public class MdfNickNameHandler extends AuthenticatedHttpHandler {
    private final SettingService settingService = new SettingService();
    @Override
    protected void handleAuthenticatedPostRequest(HttpExchange exchange, Map<String, Object> requestBody, Long userId) throws IOException {
        String newNickname = (String) requestBody.get("newNickname");
        String password = (String) requestBody.get("password");

        if (newNickname == null || newNickname.trim().isEmpty() || password == null || password.isEmpty()) {
            sendErrorResponse(exchange, 400, "新昵称和密码不能为空。");
            return;
        }

        try {
            boolean success = settingService.updateNickname(userId, newNickname.trim(), password);
            if (success) {
                sendResponse(exchange, 200, Map.of("message", "昵称修改成功。"));
            } else {
                sendErrorResponse(exchange, 500, "更新失败，请稍后重试。");
            }
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 401, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "服务器内部错误。");
        }
    }

}
