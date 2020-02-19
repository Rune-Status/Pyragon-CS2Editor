const electron = require('electron');
const { remote } = electron;

$(document).on('click', '#minimize-button', () => {
    remote.getCurrentWindow().minimize();
});

$(document).on('click', '#maximize-button', () => {
    if(!remote.getCurrentWindow().maximizable) return false;
    remote.getCurrentWindow().isMaximized() ? 
        remote.getCurrentWindow().unmaximize() :
        remote.getCurrentWindow().maximize();
});

$(document).on('click', '#exit-button', () => {
    remote.getCurrentWindow().close();
});

function sendAlert(text) {
    noty({
        text,
        layout: 'topRight',
        timeout: 5000,
        theme: 'cryogen'
    });
}