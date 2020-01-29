const root = require('app-root-path');
let config = require(root+'/config.json');
const { app, BrowserWindow } = require('electron');

let window;

function createWindow() {
    window = new BrowserWindow({
        width: 800,
        height: 600,
        webPreferences: {
            nodeIntegration: true
        }
    });

    window.loadURL('http://codysprojects.ca');

    window.on('closed', () => {
        window = null;
    });
}

app.on('ready', createWindow);

app.on('window-all-closed', () => {
    if(process.platform !== 'darwin') app.quit();
});

app.on('activate', () => {
    if(window === null) createWindow();
});