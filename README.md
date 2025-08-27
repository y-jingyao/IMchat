# IMchat - Lightweight Instant Messaging System  
# IMchat - 轻量级即时通讯系统  

IMchat is a lightweight instant messaging (IM) desktop application built with an **Electron frontend** and a **pure Java backend**. The project aims to provide a minimal viable IM solution that does not rely on heavyweight frameworks like Spring, focusing on the implementation of core chat functionalities.  

IMchat 是一个基于 **Electron 前端** 和 **纯 Java 后端** 构建的轻量级即时通讯（IM）桌面应用。项目旨在提供一个不依赖 Spring 等重型框架的最小可行性 IM 解决方案，专注于核心聊天功能的实现。  


## ✨ Core Features  
## ✨ 核心功能  

### User System  
### 用户系统  
- Secure user registration and login  
  - 安全的用户注册与登录  
- Stateless identity authentication using JWT (JSON Web Token)  
  - 使用 JWT (JSON Web Token) 进行无状态身份认证  
- Support for personal information management (e.g., nickname modification)  
  - 支持修改昵称等个人信息管理  


### Real-Time Communication  
### 实时通讯  
- One-on-one private chat based on WebSocket  
  - 基于 WebSocket 的一对一单聊  
- Heartbeat mechanism and automatic reconnection after disconnection to ensure connection stability  
  - 心跳机制与断线自动重连，保证连接稳定性  


### Message Management  
### 消息管理  
- Support for offline message storage and automatic push after going online  
  - 支持离线消息存储与上线后自动推送  
- Historical message loading (scroll-to-load)  
  - 历史消息记录加载（滚动加载）  


### Friend System  
### 好友系统  
- Search for users and send friend requests  
  - 搜索用户并发起好友请求  
- Handle friend requests (accept/reject)  
  - 处理好友请求（接受/拒绝）  
- Display friend list and delete friends  
  - 好友列表展示与删除好友  


## 🛠️ Tech Stack  
## 🛠️ 技术栈  

### Backend (Java)  
### 后端 (Java)  
- HTTP Service: com.sun.net.httpserver.HttpServer (JDK Native)  
  - HTTP 服务: com.sun.net.httpserver.HttpServer（JDK 原生）  
- WebSocket Service: org.java-websocket/Java-WebSocket  
  - WebSocket 服务: org.java-websocket/Java-WebSocket  
- Database Connection: JDBC & mysql-connector-java  
  - 数据库连接: JDBC & mysql-connector-java  
- Password Encryption: at.favre.lib:bcrypt  
  - 密码加密: at.favre.lib:bcrypt  
- JWT Authentication: com.auth0:java-jwt  
  - JWT 认证: com.auth0:java-jwt  
- JSON Processing: com.google.code.gson:gson  
  - JSON 处理: com.google.code.gson:gson  
- Build Tool: Maven  
  - 构建工具: Maven  


### Frontend (Electron)  
### 前端 (Electron)  
- Framework: Electron  
  - 框架: Electron  
- Core Technologies: HTML5, CSS3, JavaScript (ES6+)  
  - 核心技术: HTML5, CSS3, JavaScript (ES6+)  
- Communication Protocols: WebSocket, HTTP (Fetch API)  
  - 通信协议: WebSocket, HTTP (Fetch API)  


### Database  
### 数据库  
- MySQL 9.0+  


## 🚀 How to Run  
## 🚀 如何运行  

### 1. Environment Preparation  
### 1. 环境准备  
- Install JDK 23 or higher  
  - 安装 JDK 23 或更高版本  
- Install Maven 3.6+  
  - 安装 Maven 3.6+  
- Install Node.js 16+ and npm  
  - 安装 Node.js 16+ 和 npm  
- Install MySQL 9.0+  
  - 安装 MySQL 9.0+  


### 2. Database Setup  
### 2. 数据库设置  
- Log in to your MySQL server.  
  - 登录你的 MySQL 服务器。  
- Execute the SQL script in the project folder to create the database and required data tables.  
  - 执行文件夹中 SQL 脚本，创建数据库和所需的数据表。  


### 3. Run the Backend Service  
### 3. 运行后端服务  
- Navigate to the backend project directory.  
  - 进入后端项目目录。  
- Modify the `src/main/java/com/example/im/db/DatabaseManager.java` file, replacing the database username and password with your own configuration.  
  - 修改 `src/main/java/com/example/im/db/DatabaseManager.java` 文件，将数据库用户名和密码替换为你自己的配置。  
- Compile and package the project using Maven:  
  - 使用 Maven 编译和打包项目：  
    ```bash
    mvn clean package
    ```  
- Run the generated JAR file:  
  - 运行生成的 JAR 文件：  
    ```bash
    java -jar target/im-java-backend-1.0-SNAPSHOT.jar
    ```  
- The backend starts successfully when you see the console output: *"WebSocket server started..."* and *"HTTP API server started..."*.  
  - 看到控制台输出 *"WebSocket server started..."*（WebSocket 服务器已启动...）和 *"HTTP API server started..."*（HTTP API 服务器已启动...）即表示后端启动成功。  


### 4. Run the Frontend Application  
### 4. 运行前端应用  
- Navigate to the frontend project directory.  
  - 进入前端项目目录。  
- Install project dependencies:  
  - 安装项目依赖：  
    ```bash
    npm install
    ```  
- Start the Electron application:  
  - 启动 Electron 应用：  
    ```bash
    npm start
    ```  
- After the application starts, you can register a new user and start using it!  
  - 应用启动后，你就可以注册新用户并开始使用了！  


## 🖼️ Project Screenshots  
## 🖼️ 项目截图  

![login](./sotp/login.png)  
![register](./sotp/register.png)  
![chat](./sotp/chat.png)  
![contact](./sotp/contact.png)  
![setting](./sotp/setting.png)  


## 💡 Future Plans  
## 💡 未来计划  
- [ ] Group chat functionality  
  - [ ] 群聊功能  
- [ ] File and image transfer  
  - [ ] 文件与图片传输  
- [ ] User online status display  
  - [ ] 用户在线状态显示  
- [ ] End-to-end encryption  
  - [ ] 端到端加密  
- [ ] More comprehensive user information display  
  - [ ] 更完善的用户信息展示  


## 📄 Open Source License  
## 📄 开源协议  

This project adopts the **MIT License**.  
本项目采用 **MIT License** 开源协议。
