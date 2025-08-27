document.addEventListener('DOMContentLoaded', () => {
    // 服务端连接配置
    const WEBSOCKET_URL = 'ws://localhost:8081';  // WebSocket地址
    const API_BASE_URL = 'http://localhost:8080/api';  // 后端API基础地址

    // 从本地存储获取用户认证信息
    const token = localStorage.getItem('im-token');  // 用户身份令牌
    const currentUser = JSON.parse(localStorage.getItem('im-user'));  // 当前用户信息

    // 验证用户登录状态，未登录则提示并终止初始化
    if (!token || !currentUser) {
        showAlert('请先登录！').then();
        return;
    }

    //==============================================获取DOM元素=========================================================//
    // 连接状态显示元素
    const connectionStatusEl = document.getElementById('connection-status');
    const settingStatusEl = document.getElementById('setting-status');
    // 消息相关元素
    const messagesContainer = document.getElementById('messages-container');
    const messageInputBox = document.getElementById('message-input-box');
    const sendButton = document.getElementById('send-button');
    // 用户搜索相关元素
    const searchInput = document.getElementById('search-user-input');
    const searchButton = document.getElementById('search-user-button');
    const searchResultsContainer = document.getElementById('search-results-container');
    // 好友和请求列表元素
    const requestsListEl = document.getElementById('requests-list');
    const friendListEl = document.getElementById('friend-list');
    // 导航和视图容器
    const navChatBtn = document.getElementById('nav-chat-btn');
    const navContactsBtn = document.getElementById('nav-contacts-btn');
    const navSettingBtn = document.getElementById('nav-setting-btn');
    const chatView = document.getElementById('chat-view');
    const contactsView = document.getElementById('contacts-view');
    const settingsView = document.getElementById('settings-view');
    const recentChatsListEl = document.getElementById('recent-chats-list');
    // setting界面元素
    const logoutBtn = document.getElementsByClassName('setting-logout-btu');
    const mdfNicknameEl = document.getElementById('setting-mdfNickname');
    const mdfUsernameEl = document.getElementById('setting-mdfUsername');
    const mdfPasswordEl = document.getElementById('setting-mdfPassword');
    //================================================================================================================//
    // ==========================================初始化用户信息展示=======================================================//
    // 设置聊天界面的当前用户信息
    document.getElementById('current-username').textContent = currentUser.username;
    document.getElementById('current-nickname').textContent = currentUser.nickname;
    // 设置setting界面用户信息
    document.getElementById('setting-username').textContent = "账号：" + currentUser.username;
    document.getElementById('setting-nickname').textContent = "昵称：" + currentUser.nickname;
    document.getElementById('setting-uid').textContent = "账号ID：" + currentUser.uid;
    document.getElementById('setting-creatAt').textContent = "创建时间：" + currentUser.creat_at;
    document.getElementById('setting-status0').textContent = "账号状态：" + currentUser.status0;

    //==================================================全局变量========================================================//
    let ws;
    let heartbeatInterval;
    let currentChatTarget = null;
    const Long = {MAX_VALUE: '9223372036854775807'};  // 自定义长整数最大值用于消息ID处理(JS没有长整型,为了和后端对齐)
    let oldestMessageId = Long.MAX_VALUE;
    let isLoadingHistory = false;
    //================================================================================================================//
    //===================================================视图切换逻辑===================================================//
    //视图切换函数
    function switchView(viewId) {
        // 隐藏所有视图并移除导航按钮选中状态
        document.querySelectorAll('.view').forEach(view => view.classList.remove('active'));
        document.querySelectorAll('.navigation-menu button').forEach(btn => btn.classList.remove('active'));
        // 显示目标视图并激活对应导航按钮
        if (viewId === 'chat') {
            chatView.classList.add('active');
            navChatBtn.classList.add('active');
        } else if (viewId === 'contacts') {
            contactsView.classList.add('active');
            navContactsBtn.classList.add('active');
        } else if (viewId === 'settings') {
            settingsView.classList.add('active');
            navSettingBtn.classList.add('active');
        }
    }

    //================================================================================================================//
    //============================================WebSocket通信========================================================//
    //建立WebSocket连接
    function connect() {
        // 创建WebSocket实例，携带认证token
        ws = new WebSocket(`${WEBSOCKET_URL}?token=${encodeURIComponent(token)}`);
        // 连接成功回调
        ws.onopen = () => {
            console.log('WebSocket connected.');
            // 更新连接状态为在线
            // 聊天界面
            connectionStatusEl.textContent = '●在线';
            connectionStatusEl.style.color = 'green';
            // 设置界面
            settingStatusEl.textContent = '●在线';
            settingStatusEl.style.color = 'green';
            // 启动心跳检测
            startHeartbeat();
        };
        // 接收消息回调
        ws.onmessage = (event) => {
            const message = JSON.parse(event.data);
            handleRealtimeMessage(message);
        };
        // 连接关闭回调
        ws.onclose = () => {
            console.log('WebSocket disconnected.');
            // 更新连接状态为离线
            // 聊天界面
            connectionStatusEl.textContent = '●离线';
            connectionStatusEl.style.color = 'red';
            // 设置界面
            settingStatusEl.textContent = '●离线';
            settingStatusEl.style.color = 'red';
            // 停止心跳并尝试重连
            stopHeartbeat();
            setTimeout(connect, 5000);  // 5秒后自动重连
        };
        // 连接错误回调
        ws.onerror = (error) => {
            console.error('WebSocket error:', error);
            connectionStatusEl.textContent = '连接错误';
            connectionStatusEl.style.color = 'red';
            ws.close();
        };
    }

    // 启动心跳检测
    function startHeartbeat() {
        stopHeartbeat();  // 先停止已有心跳
        heartbeatInterval = setInterval(() => {
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify({type: 'ping'}));
            }
        }, 30000); // 30秒发一次
    }

    // 停止心跳检测
    function stopHeartbeat() {
        clearInterval(heartbeatInterval);
    }

    //================================================================================================================//
    //==================================================API===========================================================//
    // 封装API请求，统一处理认证和错误
    async function apiFetch(endpoint, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        };
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {...defaultOptions, ...options});
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || `API Error: ${response.status}`);
        }
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
            return response.json();
        }
    }

    //================================================================================================================//
    //==============================================好友管理逻辑=========================================================//
    // 加载好友列表和好友请求
    async function loadFriendsAndRequests() {
        try {
            const data = await apiFetch('/friends/list', {method: 'GET'});
            renderFriendList(data.friends);  // 瞎几把乱报错
            renderRequestList(data.requests);
        } catch (error) {
            console.error('Failed to load friends and requests:', error);
        }
    }

    // 渲染好友列表
    function renderFriendList(friends) {
        friendListEl.innerHTML = '';
        if (friends.length === 0) {
            friendListEl.innerHTML = '<li>暂无好友</li>';
            return;
        }
        // 渲染好友列表
        friends.forEach(friend => {
            const li = document.createElement('li');
            li.innerHTML = `
                <span>${friend.nickname}(${friend.username})</span>
                <button class="delete-friend-btn" data-friend-id="${friend.uid}">删除</button>
            `;
            li.querySelector('span').addEventListener('click', () => startChat(friend));
            friendListEl.appendChild(li);
        });
    }

    // 渲染好友请求列表
    function renderRequestList(requests) {
        requestsListEl.innerHTML = '';
        if (requests.length === 0) {
            requestsListEl.innerHTML = '<li>没有新的好友请求</li>';
            return;
        }
        requests.forEach(request => {
            const li = document.createElement('li');
            li.innerHTML = `
                <span>${request.nickname} (${request.username})</span>
                <div>
                    <button class="accept-btn" data-friend-id="${request.uid}">接受</button>
                    <button class="decline-btn" data-friend-id="${request.uid}">拒绝</button>
                </div>
            `;
            requestsListEl.appendChild(li);
        });
    }

    // 搜索用户
    async function handleSearch() {
        const searchTerm = searchInput.value.trim();
        if (!searchTerm) return;
        try {
            const users = await apiFetch(`/friends/search?username=${encodeURIComponent(searchTerm)}`, {method: 'GET'});
            searchResultsContainer.innerHTML = '';
            if (users.length === 0) {
                searchResultsContainer.innerHTML = '<p>未找到用户</p>';
                return;
            }
            users.forEach(user => {
                const div = document.createElement('div');

                div.classList.add('search-result-item');

                let buttonHtml;

                if (user.status === 0) {
                    buttonHtml = `<button class="add-friend-btn" data-friend-id="${user.uid}" disabled>等待验证</button>`;
                } else if (user.status === 1) {
                    buttonHtml = `<button class="add-friend-btn" data-friend-id="${user.uid}" disabled>已添加</button>`;
                } else {
                    buttonHtml = `<button class="add-friend-btn" data-friend-id="${user.uid}">添加</button>`;
                }
                div.innerHTML = `<span>${user.nickname} (${user.username})</span>${buttonHtml}`;
                searchResultsContainer.appendChild(div);
            });
        } catch (error) {
            console.error('Search failed:', error);
            searchResultsContainer.innerHTML = `<p>${error.message}</p>`;
        }
    }

    // 处理好友相关操作
    async function handleFriendAction(e) {
        const target = e.target;

        try {
            // 添加好友
            if (target.classList.contains('add-friend-btn')) {
                await handleAddFriend(target);
            }
            // 接受好友请求
            else if (target.classList.contains('accept-btn')) {
                await handleAcceptFriend(target);
            }
            // 拒绝好友请求或删除好友
            else if (target.classList.contains('decline-btn') || target.classList.contains('delete-friend-btn')) {
                await handleRemoveFriend(target);
            }
        } catch (error) {
            console.error('Friend action failed:', error);
            await showAlert(`操作失败: ${error.message}`);
        }
    }

    // 处理添加好友
    async function handleAddFriend(target) {
        const friendId = target.dataset.friendId;
        await apiFetch('/friends/add', {
            method: 'POST',
            body: JSON.stringify({friendId: Number(friendId)})
        });
        await showAlert('好友请求已发送！');
        target.textContent = '已发送';
        target.disabled = true;
    }

    // 处理接受好友请求
    async function handleAcceptFriend(target) {
        const friendId = target.dataset.friendId;
        await apiFetch('/friends/accept', {
            method: 'POST',
            body: JSON.stringify({friendId: Number(friendId)})
        });
        await showAlert('已添加好友！');
        await loadFriendsAndRequests();
    }

    // 处理拒绝或删除好友
    async function handleRemoveFriend(target) {
        const friendId = target.dataset.friendId;
        const actionType = target.classList.contains('delete-friend-btn') ? '删除' : '拒绝';
        const confirmDelete = await showConfirm(`确定要${actionType}该好友吗？`);

        if (confirmDelete) {
            await apiFetch('/friends/delete', {
                method: 'POST',
                body: JSON.stringify({friendId: Number(friendId)})
            });
            await showAlert('操作成功！');
            await loadFriendsAndRequests();

            // 移除最近聊天列表中的对应项
            const recentChatLi = recentChatsListEl.querySelector(`li[data-uid='${friendId}']`);
            if (recentChatLi) {
                recentChatLi.remove();
            }

            // 如果正在与该好友聊天，重置聊天界面
            if (currentChatTarget && currentChatTarget.uid === Number(friendId)) {
                resetChatInterface();
            }
        }
    }

    // 重置聊天界面
    function resetChatInterface() {
        currentChatTarget = null;
        document.getElementById('chat-with-user').textContent = '选择一个好友开始聊天';
        messagesContainer.innerHTML = '';
        messageInputBox.disabled = true;
        sendButton.disabled = true;
    }

    //================================================================================================================//
    //===============================================聊天功能逻辑========================================================//
    // 开始与指定好友聊天
    async function startChat(friend) {

        switchView('chat');

        if (currentChatTarget && currentChatTarget.uid === friend.uid) {
            return;
        }

        // 更新最近聊天列表
        const existingChatLi = recentChatsListEl.querySelector(`li[data-uid='${friend.uid}']`);
        if (existingChatLi) {
            recentChatsListEl.prepend(existingChatLi);
        } else {
            const li = document.createElement('li');
            li.dataset.uid = friend.uid;
            li.dataset.username = friend.username;
            li.innerHTML = `<span>${friend.nickname}</span>`;
            li.addEventListener('click', () => startChat(friend));
            recentChatsListEl.prepend(li);
        }

        // 更新最近聊天列表的选中状态
        document.querySelectorAll('#recent-chats-list li').forEach(li => li.classList.remove('active'));
        const currentChatLi = recentChatsListEl.querySelector(`li[data-uid='${friend.uid}']`);
        if (currentChatLi) {
            currentChatLi.classList.add('active');
            currentChatLi.classList.remove('new-message');
        }

        // 初始化聊天界面
        currentChatTarget = friend;
        document.getElementById('chat-with-user').textContent = `与 ${friend.nickname} 聊天中`;
        messageInputBox.disabled = false;
        sendButton.disabled = false;
        messagesContainer.innerHTML = '';
        messageInputBox.focus();

        // 加载历史消息
        oldestMessageId = Long.MAX_VALUE;
        isLoadingHistory = false;
        await loadHistory();
    }

    // 加载历史消息
    async function loadHistory() {
        if (!currentChatTarget || isLoadingHistory) {
            return;
        }

        isLoadingHistory = true;

        try {
            const history = await apiFetch(
                `/messages/history?friendId=${currentChatTarget.uid}&before=${oldestMessageId}&limit=20`,
                {method: 'GET'}
            );

            if (history.length > 0) {
                oldestMessageId = history[0].mid;
                const oldScrollHeight = messagesContainer.scrollHeight;
                renderMessages(history, 'prepend');
                const newScrollHeight = messagesContainer.scrollHeight;
                messagesContainer.scrollTop = newScrollHeight - oldScrollHeight;
            }
        } catch (error) {
            console.error('Failed to load message history:', error);
        } finally {
            isLoadingHistory = false;
        }
    }

    // 渲染消息列表
    function renderMessages(messages, method = 'append') {
        const fragment = document.createDocumentFragment();

        messages.forEach(message => {
            const messageEl = createMessageElement(message);
            fragment.appendChild(messageEl);
        });

        if (method === 'prepend') {
            messagesContainer.prepend(fragment);
        } else {
            messagesContainer.appendChild(fragment);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }

    // 创建单个消息元素
    function createMessageElement(message) {
        const messageEl = document.createElement('div');
        messageEl.classList.add('message-item');

        // 根据发送者判断消息对齐方式
        if (message.senderId === currentUser.uid) {
            messageEl.classList.add('sent');
        } else {
            messageEl.classList.add('received');
        }

        const senderUsername = message.senderId === currentUser.uid ? '我' : (currentChatTarget ? currentChatTarget.username : '对方');

        messageEl.innerHTML = `
            <div class="message-sender">${senderUsername} <span class="message-time">${new Date(message.createdAt).toLocaleString()}</span></div>
            <div class="message-content">${message.content}</div>
        `;

        return messageEl;
    }

    // 处理实时收到的消息
    function handleRealtimeMessage(message) {

        if (message.type === 'pong') return;

        // 当前聊天对象的消息直接显示
        if (currentChatTarget && message.senderId === currentChatTarget.uid) {
            renderMessages([message], 'append');
        } else {
            // 否则更新最近聊天列表的新消息提示
            const senderLi = recentChatsListEl.querySelector(`li[data-uid='${message.senderId}']`);
            if (senderLi) {
                senderLi.classList.add('new-message');
                recentChatsListEl.prepend(senderLi);
            }
        }
    }

    //================================================================================================================//
    //=================================================自定义弹窗=======================================================//
    // 显示自定义提示框
    function showAlert(message) {
        const overlay = document.getElementById('custom-alert-overlay');
        const messageEl = document.getElementById('custom-alert-message');
        const okBtn = document.getElementById('custom-alert-ok');

        messageEl.innerText = message;
        overlay.style.display = 'flex';

        return new Promise(resolve => {
            okBtn.onclick = () => {
                overlay.style.display = 'none';
                resolve();
            };
        });
    }

    // 显示自定义确认框
    function showConfirm(message) {
        const overlay = document.getElementById('custom-confirm-overlay');
        const messageEl = document.getElementById('custom-confirm-message');
        const okBtn = document.getElementById('custom-confirm-ok');
        const cancelBtn = document.getElementById('custom-confirm-cancel');

        messageEl.innerText = message;
        overlay.style.display = 'flex';

        return new Promise(resolve => {
            okBtn.onclick = () => {
                overlay.style.display = 'none';
                resolve(true);
            };

            cancelBtn.onclick = () => {
                overlay.style.display = 'none';
                resolve(false);
            };
        });
    }

    //显示自定义带有输入框的弹窗
    function showInput(message0, message1) {
        const overlay = document.getElementById('setting-mdf-overlay');
        const messageEl = document.getElementById('setting-mdf-message');
        const inputDataEl = document.getElementById('setting-input-data');
        const inputPwdEl = document.getElementById('setting-input-password');
        const okBtn = document.getElementById('setting-mdf-ok');
        const cancelBtn = document.getElementById('setting-mdf-cancel');
        let cfmPasswordEL;

        if (message0 === '修改密码') {
            const form = document.getElementById('login-form');
            const passwordInput = document.getElementById('setting-input-password');
            inputDataEl.type = 'password';

            if (!document.getElementById('middle-input')) {
                const confirmPwdInput = document.createElement('input');
                confirmPwdInput.type = 'password';
                confirmPwdInput.id = 'middle-input';
                confirmPwdInput.placeholder = '确认新密码';
                confirmPwdInput.required = true;
                form.insertBefore(confirmPwdInput, passwordInput);
            }
            cfmPasswordEL = document.getElementById('middle-input');
        }


        messageEl.innerText = message0;
        inputDataEl.placeholder = message1;
        inputDataEl.value = '';
        inputPwdEl.value = '';
        if (cfmPasswordEL) cfmPasswordEL.value = '';
        overlay.style.display = 'flex';


        return new Promise(resolve => {

            const handleOkClick = () => {
                const newData = inputDataEl.value.trim();
                const passwd = inputPwdEl.value.trim();

                if (message0 === "修改账号" && (newData.length < 5 || newData.length > 6)) {
                    showAlert("账号必须5-6位").then();
                    overlay.style.display = 'none';
                    resolve({confirm: false});
                }
                if (cfmPasswordEL) {
                    const cfmPasswd = cfmPasswordEL.value.trim();
                    if (newData.length < 6) {
                        showAlert("密码不能少于6位").then();
                        overlay.style.display = 'none';
                        resolve({confirm: false});
                    }
                    if (newData !== cfmPasswd) {
                        showAlert("两次输入的新密码不一致").then();
                        overlay.style.display = 'none';
                        resolve({confirm: false});
                    }
                }

                overlay.style.display = 'none';
                resolve({
                    confirm: true,
                    inputValue: newData,
                    password: passwd
                });
                if (cfmPasswordEL) {
                    cfmPasswordEL.remove();
                }
                okBtn.removeEventListener('click', handleOkClick);
            };

            const handleCancelClick = () => {
                overlay.style.display = 'none';
                resolve({confirm: false});
                if (cfmPasswordEL) {
                    cfmPasswordEL.remove();
                }
                cancelBtn.removeEventListener('click', handleCancelClick);
            };

            const handleOverlayClick = (e) => {
                if (e.target === overlay) {
                    overlay.style.display = 'none';
                    resolve({confirm: false});
                    if (cfmPasswordEL) {
                        cfmPasswordEL.remove();
                    }
                    overlay.removeEventListener('click', handleOverlayClick);
                }
            };

            okBtn.addEventListener('click', handleOkClick);
            cancelBtn.addEventListener('click', handleCancelClick);
            overlay.addEventListener('click', handleOverlayClick);
        });
    }


    //================================================================================================================//
    //================================================发送消息功能=======================================================//
    // 发送消息
    function sendMessage() {
        const content = messageInputBox.value.trim();

        if (content && currentChatTarget && ws && ws.readyState === WebSocket.OPEN) {
            // 构建消息对象
            const message = {
                receiverId: currentChatTarget.uid,
                content: content,
            };

            // 发送消息到服务器
            ws.send(JSON.stringify(message));

            // 本地显示已发送消息
            renderMessages([{
                senderId: currentUser.uid,
                content: content,
                createdAt: new Date().toISOString()
            }], 'append');

            // 清空输入框
            messageInputBox.value = '';
        }
    }

    //================================================================================================================//
    //================================================设置界面功能======================================================//
    //退出登录
    function logout() {
        ws.close();
        window.electronAPI.navigateToLogin(); // 瞎几把乱报错
    }

    //修改信息
    async function mdfData(type) {
        // 根据不同类型设置弹窗信息和验证规则
        let title, placeholder, apiEndpoint, dataKey;

        switch (type) {
            case 'nickname':
                title = '修改昵称';
                placeholder = '请输入新昵称';
                apiEndpoint = '/setting/nickname';
                dataKey = 'newNickname';
                break;
            case 'username':
                title = '修改账号';
                placeholder = '请输入新账号';
                apiEndpoint = '/setting/username';
                dataKey = 'newUsername';
                break;
            case 'password':
                title = '修改密码';
                placeholder = '请输入新密码';
                apiEndpoint = '/setting/password';
                dataKey = 'newPassword';
                break;
            default:
                await showAlert('未知的修改类型');
                return;
        }

        try {
            // 显示弹窗并获取用户输入
            const {confirm, inputValue: newValue, password} = await showInput(
                title,
                placeholder
            );

            // 若用户取消，直接返回
            if (!confirm) return;

            // 输入合法性校验
            if (!newValue) {
                await showAlert('新值不能为空！');
                return;
            }
            if (!password) {
                await showAlert('请输入当前密码验证身份！');
                return;
            }
            // 构建请求数据
            const requestData = {
                [dataKey]: newValue,
                password: password
            };

            // 调用后端API
            await apiFetch(apiEndpoint, {
                method: 'POST',
                body: JSON.stringify(requestData)
            })

            // 更新页面显示和本地存储
            updateUIAndStorage(type, newValue);

            // 提示修改成功
            if (type === "password") {
                await showAlert('密码修改成功！请重新登录');
                logout();
            } else {
                await showAlert(`${getTypeText(type)}修改成功！`);
            }

        } catch (error) {
            console.error(`${getTypeText(type)}修改失败：`, error);
            await showAlert(`${getTypeText(type)}修改失败：${error.message || '网络异常，请重试'}`);
        }
    }

// 获取类型的中文文本
    function getTypeText(type) {
        const typeMap = {
            'nickname': '昵称',
            'username': '账号',
            'password': '密码'
        };
        return typeMap[type] || '信息';
    }

// 更新UI和本地存储
    function updateUIAndStorage(type, value) {
        // 更新当前用户对象
        currentUser[type] = value;
        // 更新本地存储
        localStorage.setItem('im-user', JSON.stringify(currentUser));
        // 更新UI显示
        switch (type) {
            case 'nickname':
                document.getElementById('current-nickname').textContent = value;
                document.getElementById('setting-nickname').textContent = `昵称：${value}`;
                break;
            case 'username':
                document.getElementById('current-username').textContent = value;
                document.getElementById('setting-username').textContent = `账号：${value}`;
                break;
        }
    }

    //================================================================================================================//
    //================================================事件监听绑定======================================================//
    // 搜索事件
    searchButton.addEventListener('click', handleSearch);
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleSearch().then();
    });

    // 好友事件
    document.body.addEventListener('click', handleFriendAction);

    // 发送消息相关事件
    sendButton.addEventListener('click', sendMessage);
    messageInputBox.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // 滚动加载历史消息
    messagesContainer.addEventListener('scroll', () => {
        if (messagesContainer.scrollTop === 0 && !isLoadingHistory) {
            loadHistory().then();
        }
    });

    // 导航按钮事件
    navChatBtn.addEventListener('click', () => switchView('chat'));
    navContactsBtn.addEventListener('click', () => {
        switchView('contacts');
        loadFriendsAndRequests().then();
    });
    navSettingBtn.addEventListener('click', () => switchView('settings'));

    // 退出登录事件
    logoutBtn[0].addEventListener('click', async () => {
        //弹窗确定后退出登录
        const confirmLogout = await showConfirm(`您确定要退出登录吗？`);
        if (confirmLogout) {
            logout();
        }
    });
    // 修改昵称事件
    mdfNicknameEl.addEventListener('click', async () => await mdfData('nickname'));
    mdfUsernameEl.addEventListener('click', async () => await mdfData('username'));
    mdfPasswordEl.addEventListener('click', async () => await mdfData('password'));

    //================================================================================================================//
    //===================================================初始化========================================================//
    connect();  // 建立WebSocket连接
    loadFriendsAndRequests().then();  // 加载好友和请求列表
});
