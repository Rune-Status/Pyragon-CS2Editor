const FILE_OPTIONS = [
    {
        name: 'Save and Repack',
        onClick: () => {
            closeOptionsMenu();
            $.post('/interface-editor/save-interface/' + currentInterface, { }, ret => {
                let data = JSON.parse(ret);
                if(data.error) {
                    sendAlert(data.error);
                    return false;
                }
                sendAlert('Interface has been saved successfully.');
            });
      }
    }
];

const EDIT_OPTIONS = [

];

const OPEN_OPTIONS = [{
        name: 'Open CS2Editor',
        onClick: () => {
            closeOptionsMenu();
            renderer.send('ide:open');
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

];