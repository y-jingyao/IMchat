package im.handler.setting;

import im.handler.AuthenticatedHttpHandler;
import im.service.SettingService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public class MdfUserNameHandler extends AuthenticatedHttpHandler {
    private SettingService settingService = new SettingService();
    @Override
    protected void handleAuthenticatedPostRequest(HttpExchange exchange, Map<String, Object> requestBody, Long userId) throws IOException {
        String newUsername = (String) requestBody.get("newUsername");
        String password = (String) requestBody.get("password");
        if(newUsername == null || newUsername.trim().isEmpty() || password == null  || password.isEmpty())
        {
            sendErrorResponse(exchange, 400, "新账号和密码不能为空");
            return;
        }
        try{
            boolean success = settingService.updateUsername(userId, newUsername, password);
            if(success){
                sendResponse(exchange,200,Map.of("message","账号修改成功"));
            }
            else{
                sendErrorResponse(exchange,500,"更新失败");
            }
        }catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 401, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "服务器内部错误。");
        }

    }
}
