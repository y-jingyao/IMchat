// 等待DOM完全加载后执行代码，确保所有HTML元素都已可用
document.addEventListener('DOMContentLoaded', () => {
    //==============================================获取DOM元素=========================================================//
    // 获取页面中所有需要操作的DOM元素
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const loginContainer = document.getElementById('login-container');
    const registerContainer = document.getElementById('register-container');
    const showRegisterLink = document.getElementById('show-register');
    const showLoginLink = document.getElementById('show-login');
    const errorMessage = document.getElementById('error-message');
    // 后端API基础地址
    const API_BASE_URL = 'http://localhost:8080/api';
    //================================================================================================================//

    //===============================================界面切换===========================================================//
    // 切换为注册界面
    showRegisterLink.addEventListener('click', (e) => {
        e.preventDefault();
        loginContainer.style.display = 'none';
        registerContainer.style.display = 'block';
        errorMessage.textContent = '';
    });
    // 切换为登录界面
    showLoginLink.addEventListener('click', (e) => {
        e.preventDefault();
        registerContainer.style.display = 'none';
        loginContainer.style.display = 'block';
        errorMessage.textContent = '';
    });
    //================================================================================================================//

    //================================================表单提交==========================================================//
    // 登录表单提交事件处理
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        // 获取用户输入的用户名和密码
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;
        try {
            // 发送登录请求到后端API
            const response = await fetch(`${API_BASE_URL}/login`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username, password}),    // 将用户名密码转为JSON字符串
            });
            // 解析后端返回的JSON数据
            const data = await response.json();
            // 检查请求是否成功
            if (response.ok) {
                localStorage.setItem('im-token', data.token); // 保存JWT令牌（用于后续接口鉴权）
                // 保存用户信息
                localStorage.setItem('im-user', JSON.stringify({
                    uid: data.uid,
                    username: data.username,
                    nickname: data.nickname,
                    creat_at: data.creat_at,
                    status0: data.status
                }));
                // 登录成功 调用Electron API跳转到聊天页面（编译器可能会出现警告（可能是因为不支持electron），不影响功能）
                window.electronAPI.navigateToChat();
            } else {
                // 登录失败
                errorMessage.textContent = data.error || '登录失败';
            }
        } catch (error) {
            // 网络错误处理
            errorMessage.textContent = '网络错误，请稍后再试。';
            console.error('登录请求失败:', error);
        }
    });

    // 注册表单提交事件处理
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault(); // 阻止表单默认提交行为
        // 获取用户输入的注册信息
        const nickname = document.getElementById('register-nickname').value;
        const username = document.getElementById('register-username').value;
        const password = document.getElementById('register-password').value;
        const passwdcfm = document.getElementById('register-passwdcfm').value;
        // 前端表单验证
        if (username.length < 5 || username.length > 6) {
            errorMessage.textContent = '账号长度必须5-6位';
        }
        if (passwdcfm !== password) {
            errorMessage.textContent = '两次输入的密码不一致！';
        } else if (password.length < 6) {
            errorMessage.textContent = '密码长度过短（至少6位）！'
        } else {
            // 验证通过后发送注册请求到后端API
            try {
                const response = await fetch(`${API_BASE_URL}/register`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({username, password, nickname}), // 注册信息JSON化
                });
                const data = await response.json();

                if (response.ok) {
                    // 注册成功：提示用户并自动切换到登录表单
                    errorMessage.textContent = '注册成功！请登录';
                    // 3秒后自动切换到登录页面（这个界面没有弹窗，为了让用户看到注册成功的信息）
                    setTimeout(() => {
                        showLoginLink.click();
                    }, 3000);
                    // 自动填充用户名到登录表单
                    document.getElementById('login-username').value = username;
                } else {
                    // 注册失败
                    errorMessage.textContent = data.error || '注册失败';
                }
            } catch (error) {
                // 网络错误处理
                errorMessage.textContent = '网络错误，请稍后再试。';
                console.error('注册请求失败:', error);
            }
        }
    });
    //================================================================================================================//
});
