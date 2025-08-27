package im;
import im.server.HttpApiServer;
import im.server.WebSocketChatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class MainServer {
    private static final Logger logger = LoggerFactory.getLogger(MainServer.class);
    private static final int HTTP_PORT = 8080;
    private static final int WEBSOCKET_PORT = 8081;

    public static void main(String[] args) {
        try {
            // 启动 WebSocket 服务器
            WebSocketChatServer wsServer = new WebSocketChatServer(new InetSocketAddress(WEBSOCKET_PORT));
            wsServer.start();
            logger.info("WebSocket server started on port: {}", WEBSOCKET_PORT);

            // 启动 HTTP API 服务器
            HttpApiServer httpApiServer = new HttpApiServer(HTTP_PORT);
            httpApiServer.start();
            logger.info("HTTP API server started on port: {}", HTTP_PORT);

        } catch (Exception e) {
            logger.error("Failed to start servers", e);
        }
    }
}