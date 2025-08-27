package im.handler.friend;

import im.handler.AuthenticatedHttpHandler;
import im.service.FriendService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// 获取好友列表和待处理请求
public class FriendListHandler extends AuthenticatedHttpHandler {
    private final FriendService friendService = new FriendService();

    @Override
    protected void handleAuthenticatedRequest(HttpExchange exchange, Long userId) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, List<?>> friendData = friendService.getFriendsAndRequests(userId);
            sendResponse(exchange, 200, friendData);
        } else {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
        }
    }
}