const FILE_OPTIONS = [
    { 
        name: 'New Script', 
        shortcut: 'Ctrl + N' 
    },
    { 
        name: 'Save and Recompile', 
        shortcut: 'Ctrl + S' 
    }
]

const EDIT_OPTIONS = [
    {
        name: 'Edit Script Info', 
        shortcut: 'Ctrl + 1', 
        onClick: () => {
            closeOptionsMenu();
            if(currentTab == -1) {
                sendAlert('Please open a script first.');
                return false;
            }
            $.post('/ide/edit-script-info', { id: currentTab }, ret => {
                let data = JSON.parse(ret);
                if(data == null || data.error) {
                    sendAlert('Error getting script info.');
                    if(data.error) console.error(data.error);
                    return false;
                }
                n = noty({
                    text: 'Edit Script Info',
                    type: 'confirm',
                    layout: 'center',
                    dismissQueue: false,
                    template: data.html,
                    theme: 'cryogen',
                    buttons: [{
                        addClass: 'btn btn-success', text: 'Save', onClick: function ($noty) {
                            let name = $('#name').val();
                            let arguments = $('#arguments').val();
                            let variables = $('#variables').val();
                            if(arguments) {
                                console.log(arguments, arguments.split(/, ?/));
                                for (let argument of arguments.split(/, ?/)) {
                                    let split = argument.split(' ');
                                    if(split.length != 2) {
                                        sendAlert('Invalid argument: '+argument);
                                        return;
                                    }
                                }
                            }
                            if(variables) {
                                for(let variable of variables.split(', ?')) {
                                    if(variable.includes(' ')) {
                                        sendAlert('Invalid variable: '+variable);
                                        return;
                                    }
                                }
                            }
                            let returnType = $('#return-type').val();
                            $.post('/ide/save-script-info', { id: currentTab, name, arguments, variables, returnType }, ret => {
                                let data = JSON.parse(ret);
                                if(data == null || data.error) {
                                    if(data != null) sendAlert(data.error);
                                    return false;
                                }
                                closeNoty($noty);
                                sendAlert('Successfully saved script info. Reloading.');
                                let tab = currentTab;
                                closeTab(tab, true);
                                addTab(name, tab);
                            });
                        }
                    }, {
                        addClass: 'btn btn-danger', text: 'Cancel', onClick: closeNoty
                    }]
                });
            });
        }
    },
    {
        name: 'Reload Script Info',
        onClick: () => {
            closeOptionsMenu();
            $.post('/ide/reload-script-info', {}, ret => {
                let data = JSON.parse(ret);
                if(data == null || data.error) {
                    if(data.error) console.error(data.error);
                    return false;
                }
                sendAlert('Script information successfully reloaded.');
            });
        }
    }, {
        name: 'Reload Instruction Info',
        onClick: () => {
            closeOptionsMenu();
            $.post('/ide/reload-instruction-info', {}, ret => {
                let data = JSON.parse(ret);
                if (data == null || data.error) {
                    if (data.error) console.error(data.error);
                    return false;
                }
                sendAlert('Instruction information successfully reloaded.');
            });
        }
    }
];

const ABOUT_OPTIONS = [
    { name: 'Github' },
    { name: 'About' }
];