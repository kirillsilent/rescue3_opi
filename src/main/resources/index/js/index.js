import "../../jquery/jquery-3.2.1.min.js";
import {move} from "../../rs/requests.js";
import {getSettings} from "../../views.paths/paths.js";
import {setLocalStorage} from "../../utils/storage/storage.js";
import {key_tab, previous_tab} from "../../utils/storage/keys.js";
import {websocket} from "../../utils/socket/init.js";
import {welcome} from "../../utils/socket/socket.paths.js";
let ws = null;

$(document).ready(() => {
    ws = websocket(welcome);
    ws.onopen = () => {
        console.log('Connection: OK.');
    };

    ws.onmessage = (message) => {
        let obj = message.data;
        try {
            obj = JSON.parse(obj);
            if (obj.hasOwnProperty('reload')) {
                location.reload();
            }
        } catch (exception) {
        }
    }

    ws.onerror = () => {
        console.clear();
    }

})

$('#next').click(() => {
    setLocalStorage(previous_tab, 'tab_network');
    setLocalStorage(key_tab, 'tab_network');
    // Settings page will request password itself.
    move(getSettings());
});