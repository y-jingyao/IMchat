package im.handler.friend;

import im.handler.AuthenticatedHttpHandler;
import im.model.User;
import im.service.FriendService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

// 搜索用户
public class UserSearchHandler extends AuthenticatedHttpHandler {
    private final FriendService friendService = new FriendService();

    @Override
    protected void handleAuthenticatedRequest(HttpExchange exchange, Long userId) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            String searchTerm = "";
            if (query != null && query.startsWith("username=")) {
                searchTerm = URLDecoder.decode(query.substring("username=".length()), StandardCharsets.UTF_8);
            }

            if (searchTerm.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "Search term cannot be empty.");
                return;
            }

            List<User> users = friendService.searchUsers(searchTerm, userId);
            sendResponse(exchange, 200, users);
        } else {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
        }
    }
}
