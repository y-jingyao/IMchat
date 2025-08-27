package im.handler.setting;

import com.sun.net.httpserver.HttpExchange;
import im.handler.AuthenticatedHttpHandler;
import im.service.SettingService;

import java.io.IOException;
import java.util.Map;

public class MdfPasswordHandler extends AuthenticatedHttpHandler {
    private final SettingService settingService = new SettingService();
    protected void handleAuthenticatedPostRequest(HttpExchange exchange, Map<String, Object> requestBody, Long userId) throws IOException{
        String password = (String) requestBody.get("password");
        String newPassword = (String) requestBody.get("newPassword");
        if(password == null || newPassword == null || newPassword.trim().isEmpty() || password.isEmpty()){
            sendErrorResponse(exchange, 400,"新密码与密码不得为空");
            return;
        }
        if(password.equals(newPassword)){
            sendErrorResponse(exchange, 400, "新密码不得与旧密码相同");
        }
        try {
            boolean success = settingService.updatePassword(userId, newPassword, password);
            if(success){
                sendResponse(exchange, 200, Map.of("message","密码修改成功"));
            }
            else{
                sendErrorResponse(exchange, 500, "修改失败，请稍后重试");
            }
        } catch(IllegalArgumentException e){
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (Exception e){
            sendErrorResponse(exchange, 500, "服务器内部错误");
        }
    }
}
