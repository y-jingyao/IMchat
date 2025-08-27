package im.handler.friend;

import im.handler.AuthenticatedHttpHandler;
import im.service.FriendService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

// 添加好友
public class FriendAddHandler extends AuthenticatedHttpHandler {
    private final FriendService friendService = new FriendService();

    @Override
    protected void handleAuthenticatedPostRequest(HttpExchange exchange, Map<String, Object> requestBody, Long userId) throws IOException {
        Object friendIdObj = requestBody.get("friendId");
        if (!(friendIdObj instanceof Number)) {
            sendErrorResponse(exchange, 400, "Invalid friendId.");
            return;
        }
        long friendId = ((Number) friendIdObj).longValue();

        try {
            friendService.addFriend(userId, friendId);
            sendResponse(exchange, 200, Map.of("message", "Friend request sent."));
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        }
    }
}