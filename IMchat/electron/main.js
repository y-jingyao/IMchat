const {app, BrowserWindow, ipcMain, dialog} = require('electron');
const path = require('path');

let mainWindow;

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 480,
        height: 500,
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
            nodeIntegration: false,
            contextIsolation: true,
        },
    });

    // 加载登录页面
    mainWindow.loadFile('login.html');
    mainWindow.setMenu(null);
    // 打开开发者工具
    // mainWindow.webContents.openDevTools();
}

app.whenReady().then(() => {
    createWindow();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

// 监听从渲染进程发来的导航请求
ipcMain.on('navigate-to-chat', () => {
    if (mainWindow) {
        mainWindow.loadFile('chat.html');
        mainWindow.setSize(800, 600);
        mainWindow.setMinimumSize(400, 370);
    }
});

ipcMain.on('navigate-to-login', () => {
    mainWindow.loadFile('login.html');
    mainWindow.setMenu(null);
});