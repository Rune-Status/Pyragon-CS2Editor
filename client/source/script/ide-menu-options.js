const FILE_OPTIONS = [
    { 
        name: 'New Script', 
        shortcut: 'Ctrl + N' 
    },
    { 
        name: 'Save and Recompile', 
        shortcut: 'Ctrl + S',
        onClick: () => {
            closeOptionsMenu();
            let contents = editor.session.getValue();
            let script = $('.script-tab.active');
            let id = script.data('id');
            recompile(id, contents);
        }
    }
];

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
                    if(data.error) sendAlert(data.error);
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
                                for(let variable of variables.split(/, ?/)) {
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
        name: 'Edit Instruction Info',
        onClick: () => {
            closeOptionsMenu();
            noty({
                text: 'Enter the instruction opcode or name <input id="instr-id" type="text">',
                theme: 'cryogen',
                layout: 'center',
                type: 'confirm',
                dismissQueue: false,
                buttons: [{
                        addClass: 'btn btn-primary',
                        text: 'Edit',
                        onClick: function ($noty) {
                            let id = $noty.$bar.find('#instr-id').val();
                            $.post('/ide/edit-instruction-info', { id }, ret => {
                                let data = JSON.parse(ret);
                                if(data == null || data.error) {
                                    if(data.error) sendAlert(data.error);
                                    return false;
                                }
                                if(!data.exists) sendAlert('Instruction does not exist. Creating new one.');
                                closeNoty($noty);
                                let exists = data.exists;
                                n = noty({
                                    text: 'Edit Instruction Info',
                                    type: 'confirm',
                                    layout: 'center',
                                    dismissQueue: false,
                                    template: data.html,
                                    theme: 'cryogen',
                                    buttons: [{
                                        addClass: 'btn btn-success',
                                        text: 'Save',
                                        onClick: function ($noty) {
                                            let name = $('#name').val();
                                            let popOrder = $('#pop-order').val();
                                            let argNames = $('#argNames').val();
                                            let pushType = $('#push-type').val();
                                            if(name.replace(/\s/, '') == '') {
                                                sendAlert('Name must be defined!');
                                                return false;
                                            }
                                            if(pushType.replace(/\s/, '') == '') {
                                                sendAlert('Push type must be defined!');
                                                return false;
                                            }
                                            $.post('/ide/save-instruction-info', { id, name, popOrder, argNames, pushType }, ret => {
                                                let data = JSON.parse(ret);
                                                if(data == null || data.error) {
                                                    if(data.error) sendAlert(data.error);
                                                    return false;
                                                }
                                                closeNoty($noty);
                                                sendAlert('Successfully '+(exists ? 'edited' : 'created')+' instruction '+name);
                                                sendAlert('Reloading all saved scripts.');
                                                let current = currentTab;
                                                for(let id in tabs) {
                                                    if(unsaved[id]) continue;
                                                    let name = getName(id);
                                                    closeTab(id, true);
                                                    addTab(name, id);
                                                }
                                                addTab(null, current);
                                            });
                                        }
                                    }, {
                                        addClass: 'btn btn-danger',
                                        text: 'Cancel',
                                        onClick: closeNoty
                                    }]
                                });
                            });
                        }
                    },
                    {
                        addClass: 'btn btn-danger',
                        text: 'Cancel',
                        onClick: function ($noty) {
                            closeNoty($noty);
                        }
                    }
                ]
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
                    if(data.error) sendAlert(data.error);
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
                    if (data.error) sendAlert(data.error);
                    return false;
                }
                sendAlert('Instruction information successfully reloaded.');
            });
        }
    }
];

const OPEN_OPTIONS = [{
        name: 'Open Interface Editor',
        onClick: () => {
            closeOptionsMenu();
            renderer.send('interface-editor:open');
        }
    },
    {
        name: 'Open Cache Editor',
        onClick: () => {
            closeOptionsMenu();
            //Check for unedited
            //open modal asking to confirm
        }
    }
];

const ABOUT_OPTIONS = [
    { name: 'Github' },
    { name: 'About' }
];