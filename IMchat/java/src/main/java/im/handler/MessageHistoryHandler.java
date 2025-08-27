package im.handler;

import im.db.MessageRepository;
import im.model.Message;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHistoryHandler extends AuthenticatedHttpHandler {
    private final MessageRepository messageRepository = new MessageRepository();

    @Override
    protected void handleAuthenticatedRequest(HttpExchange exchange, Long userId) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        try {
            long friendId = Long.parseLong(params.getOrDefault("friendId", "0"));
            long before = Long.parseLong(params.getOrDefault("before", String.valueOf(Long.MAX_VALUE)));
            int limit = Integer.parseInt(params.getOrDefault("limit", "20"));

            if (friendId == 0) {
                sendErrorResponse(exchange, 400, "friendId is required.");
                return;
            }

            List<Message> history = messageRepository.getMessageHistory(userId, friendId, before, limit);
            sendResponse(exchange, 200, history);

        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid parameter format.");
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null) {
            return params;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                try {
                    params.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
                } catch (Exception e) {
                        //
                }
            }
        }
        return params;
    }
}
