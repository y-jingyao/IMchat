package im.handler.friend;

import im.handler.AuthenticatedHttpHandler;
import im.service.FriendService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

// 接受好友请求
public class FriendAcceptHandler extends AuthenticatedHttpHandler {
    private final FriendService friendService = new FriendService();

    @Override
    protected void handleAuthenticatedPostRequest(HttpExchange exchange, Map<String, Object> requestBody, Long userId) throws IOException {
        Object friendIdObj = requestBody.get("friendId");
        if (!(friendIdObj instanceof Number)) {
            sendErrorResponse(exchange, 400, "Invalid friendId.");
            return;
        }
        long friendId = ((Number) friendIdObj).longValue();

        boolean success = friendService.acceptFriendRequest(userId, friendId);
        if (success) {
            sendResponse(exchange, 200, Map.of("message", "Friend request accepted."));
        } else {
            sendErrorResponse(exchange, 400, "Failed to accept friend request.");
        }
    }
}
