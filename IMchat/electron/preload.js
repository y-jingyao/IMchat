const { contextBridge, ipcRenderer } = require('electron');
// 暴露 API 给渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
    navigateToChat: () => ipcRenderer.send('navigate-to-chat'),
    navigateToLogin: () => ipcRenderer.send('navigate-to-login'),
});

