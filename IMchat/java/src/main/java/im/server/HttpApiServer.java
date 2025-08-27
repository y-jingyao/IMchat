package im.server;

import im.handler.*;
import im.handler.friend.*;
import im.handler.setting.*;
import com.sun.net.httpserver.HttpServer;
import im.handler.setting.MdfNickNameHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpApiServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpApiServer.class);
    private final HttpServer server;

    public HttpApiServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        // 注册登录API
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());

        // 新增好友相关API
        server.createContext("/api/friends/list", new FriendListHandler());
        server.createContext("/api/friends/search", new UserSearchHandler());
        server.createContext("/api/friends/add", new FriendAddHandler());
        server.createContext("/api/friends/accept", new FriendAcceptHandler());
        server.createContext("/api/friends/delete", new FriendDeleteHandler());
        // 历史消息记录API
        server.createContext("/api/messages/history", new MessageHistoryHandler());
        // setting相关API
        server.createContext("/api/setting/nickname", new MdfNickNameHandler());
        server.createContext("/api/setting/username", new MdfUserNameHandler());
        server.createContext("/api/setting/password", new MdfPasswordHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
    }

    public void start() {
        server.start();
    }
}
