const electron = require('electron');
const { remote } = electron;

$(document).on('click', '#minimize-button', () => {
    remote.getCurrentWindow().minimize();
});

$(document).on('click', '#exit-button', () => {
    remote.getCurrentWindow().close();
});