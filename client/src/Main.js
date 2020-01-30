const root = require('app-root-path');
let config = require(root+'/config.json');
const { app, BrowserWindow, ipcMain } = require('electron');
const windowState = require('electron-window-state');
const request = require('request');
require('electron-debug')({
    showDevTools: true,
    enabled: true
});

let cacheSelectWindow;
let ideWindow;
let cacheLocation;
let checkCacheLoadedTimer;

function start() {
    createCacheSelectWindow();
    ipcMain.on('cache:set-location', (event, data) => {
        cacheLocation = data;
    });
    ipcMain.on('cache:load', () => {
        request.post('http://localhost:8087/cache/load', { form: { path: cacheLocation }});
        checkCacheLoadedTimer = setInterval(checkCacheLoaded, 50);
    });
}

function checkCacheLoaded() {
    request.get('http://localhost:8087/cache/loaded', {}, function(
      err,
      response,
      body
    ) {
      let data = JSON.parse(body);
      let loaded = data.loaded;
      if (data.error) {
          clearInterval(checkCacheLoadedTimer);
          cacheSelectWindow.webContents.send('cache:error', 'Error loading that cache. Please try another.');
      } else if (loaded === true) {
          clearInterval(checkCacheLoadedTimer);
          createIDEWindow();
          cacheSelectWindow.close();
          cacheSelectWindow = null;
      }
    });
}

function createIDEWindow() {
    let ideWindowState = windowState({
        defaultHeight: 500,
        defaultWidth: 800
    })
    ideWindow = new BrowserWindow({
      width: 800,
      height: 500,
      x: ideWindowState.x,
      y: ideWindowState.y,
      frame: false,
      backgroundThrottling: false,
      thickFrame: true,
      transparent: true,
      webPreferences: {
        nodeIntegration: true
      }
    });

    ideWindowState.manage(ideWindow);

    ideWindow.loadURL('http://localhost:8087/ide');

    ideWindow.on('closed', () => {
      ideWindow = null;
    });

    app.on('activate', () => {
        if (ideWindow === null) createIDEWindow();
    });
}

function createCacheSelectWindow() {
    let mainWindowState = windowState({
        defaultHeight: 245,
        defaultWidth: 400
    })
    cacheSelectWindow = new BrowserWindow({
      width: 400,
      height: 245,
      x: mainWindowState.x,
      y: mainWindowState.y,
      resizable: false,
      maximizable: false,
      fullscreenable: false,
      frame: false,
      backgroundThrottling: false,
      thickFrame: true,
      transparent: true,
      webPreferences: {
        nodeIntegration: true
      }
    });

    cacheSelectWindow.loadURL('http://localhost:8087');

    cacheSelectWindow.on('closed', () => {
      cacheSelectWindow = null;
    });

    mainWindowState.manage(cacheSelectWindow);

    app.on('activate', () => {
        if (cacheSelectWindow === null) createCacheSelectWindow();
    });
}

app.on('ready', start);

app.on('window-all-closed', () => {
    if(process.platform !== 'darwin') app.quit();
});