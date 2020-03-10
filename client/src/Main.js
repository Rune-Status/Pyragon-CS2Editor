const root = require('app-root-path');
const Store = require('electron-store');
const { app, BrowserWindow, ipcMain } = require('electron');
const windowState = require('electron-window-state');
const request = require('request');
require('electron-debug')({
    showDevTools: true,
    enabled: true
});

let store = new Store();
let cacheSelectWindow;
let ideWindow;
let interfaceEditorWindow;
let cacheLocation;
let checkCacheLoadedTimer;

function start() {
    createCacheSelectWindow();
    ipcMain.on('cache:set-hard-location', () => {
        if(!store.has('cacheLocation')) return false;
        cacheSelectWindow.webContents.send('cache:set-hard-location', store.get('cacheLocation'));
        cacheLocation = store.get('cacheLocation');
    });
    ipcMain.on('cache:set-location', (event, data) => {
        cacheLocation = data;
    });
    ipcMain.on('cache:load', (event, data) => {
        request.post('http://localhost:8087/cache/load', { form: { path: cacheLocation }});
        setTimeout(() => checkCacheLoaded(data.ide), 200);
    });
    ipcMain.on('ide:open', (event, data) => {
        if(ideWindow) return false;
        createIDEWindow();
    });
    ipcMain.on('interface-editor:open', (event, data) => {
        if(interfaceEditorWindow) return false;
        createInterfaceEditorWindow();
    });
}

function checkCacheLoaded(ide) {
    request.get('http://localhost:8087/cache/loaded', {}, function(
      err,
      response,
      body
    ) {
        if(err) {
            console.error(err);
            cacheSelectWindow.webContents.send(
              'cache:error',
              'Error loading that cache. Please try another.'
            );
            return;
        }
        let data = JSON.parse(body);
        let loaded = data.loaded;
        if (data.error)
            cacheSelectWindow.webContents.send('cache:error', 'Error loading that cache. Please try another.');
        else if (loaded === true) {
            if(cacheSelectWindow) {
                store.set('cacheLocation', cacheLocation);
                if(ide) createIDEWindow();
                else createInterfaceEditorWindow();
                cacheSelectWindow.close();
                cacheSelectWindow = null;
            }
        } else setTimeout(() => checkCacheLoaded(ide), 200);
    });
}

function createInterfaceEditorWindow() {
    let interEditorWindowState = windowState({
        defaultHeight: 615,
        defaultWidth: 1067
    })
    interfaceEditorWindow = new BrowserWindow({
        width: 1067,
        height: 615,
        resizable: false,
        x: interEditorWindowState.x,
        y: interEditorWindowState.y,
        frame: false,
        backgroundThrottling: false,
        thickFrame: true,
        transparent: true,
        webPreferences: {
            nodeIntegration: true
        }
    });

    interEditorWindowState.manage(interfaceEditorWindow);

    interfaceEditorWindow.loadURL('http://localhost:8087/interface-editor');

    interfaceEditorWindow.on('closed', () => {
        interfaceEditorWindow = null;
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

    cacheSelectWindow.on('closed', () => {
        cacheSelectWindow = null;
    });
}

app.on('ready', start);

app.on('window-all-closed', () => {
    if(process.platform !== 'darwin') app.quit();
});