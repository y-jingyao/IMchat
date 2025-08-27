package im.server;

import im.db.MessageRepository;
import im.model.Message;
import im.service.JwtService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


// WebSocket聊天服务器实现类

public class WebSocketChatServer extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketChatServer.class);

    private final Map<Long, WebSocket> onlineUsers = new ConcurrentHashMap<>();

    private final JwtService jwtService = new JwtService();

    private final MessageRepository messageRepository = new MessageRepository();

    private final Gson gson = new Gson();

    public WebSocketChatServer(InetSocketAddress address) {
        super(address);
    }

    // 当新的WebSocket连接建立时调用
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String token = extractToken(handshake.getResourceDescriptor());
        if (token == null) {

            conn.close(1008, "Token not found");
            return;
        }

        Long userId = jwtService.getUserIdFromToken(token);
        if (userId == null) {

            conn.close(1008, "Invalid Token");
            return;
        }

        onlineUsers.put(userId, conn);
        conn.setAttachment(userId);
        logger.info("User {} connected from {}", userId, conn.getRemoteSocketAddress());
        sendOfflineMessages(userId, conn);
    }

    // 向指定用户发送离线消息
    private void sendOfflineMessages(Long userId, WebSocket conn) {
        List<Message> offlineMessages = messageRepository.getOfflineMessages(userId);
        if (!offlineMessages.isEmpty()) {
            logger.info("Sending {} offline messages to user {}", offlineMessages.size(), userId);
            for (Message msg : offlineMessages) {
                conn.send(gson.toJson(msg));
                messageRepository.updateMessageStatus(msg.getMid(), 1);
            }
        }
    }


     // 处理用户下线登记
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // 从连接中获取用户ID
        Long userId = conn.getAttachment();
        if (userId != null) {
            // 从在线用户列表中移除该用户
            onlineUsers.remove(userId);
            logger.info("User {} disconnected.", userId);
        }
    }

     // 处理消息解析、转发和存储
    @Override
    public void onMessage(WebSocket conn, String message) {

        Long senderId = conn.getAttachment();
        if (senderId == null) {

            conn.close(1008, "Authentication required");
            return;
        }

        if ("{\"type\":\"ping\"}".equals(message)) {
            conn.send("{\"type\":\"pong\"}");
            return;
        }

        try {

            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

            long receiverId = jsonObject.get("receiverId").getAsLong();
            String content = jsonObject.get("content").getAsString();

            Message msg = new Message(null, senderId, receiverId, content, 0);


            WebSocket receiverConn = onlineUsers.get(receiverId);
            if (receiverConn != null && receiverConn.isOpen()) {
                msg.setStatus(1);
                receiverConn.send(gson.toJson(msg));
                logger.info("Message from {} to {} sent in real-time.", senderId, receiverId);
            } else {
                logger.info("User {} is offline. Storing message for later.", receiverId);
            }
            messageRepository.saveMessage(msg);
        } catch (JsonSyntaxException e) {
            logger.warn("Received invalid JSON message from user {}: {}", senderId, message);
        } catch (Exception e) {
            logger.error("Error processing message from user {}", senderId, e);
        }
    }


    // 处理WebSocket连接发生错误
    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("An error occurred on connection {}",
                conn != null ? conn.getRemoteSocketAddress() : "UNKNOWN", ex);
    }

    // 服务器初始化配置
    @Override
    public void onStart() {
        // 设置连接丢失超时时间（毫秒）
        setConnectionLostTimeout(100);
    }

    // 提取JWT令牌
    private String extractToken(String resourceDescriptor) {
        try {

            String query = resourceDescriptor.substring(resourceDescriptor.indexOf('?') + 1);

            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "token".equals(pair[0])) {

                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract token from URL: {}", resourceDescriptor, e);
        }
        return null;
    }
}

