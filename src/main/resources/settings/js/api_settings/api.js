import {
    sip_host,
    sip_port,
    sip_operator,
    web_host,
    web_port,
    button,
    sip_account_port,
    sip_account_host
} from "./api.check.js";
import {httpGetAsync, httpPutAsync} from "../../../rs/requests.js";
import {
    showError,
    dialog_waiting_show,
    change_dialog_waiting,
    dialog_waiting_hide,
    dialog_text, dialog
} from "../../../dialogs/js/dialog.js"
import {getAudios, getPlan, getRegRescue, getRegSip, setSipConfig} from "../../../rs/api/api.paths.js";
import {
    save_settings, text_download_audios,
    text_download_plan, text_download_audio_success,
    text_download_plan_success, text_download_checksum_not_correct
} from "../../../dialogs/js/dialog.strings.js";
import {websocket} from "../../../utils/socket/init.js";
import {create_UUID} from "../../../utils/uuid/uuid.js";

const account = $('#sip_account');
const sip_password = $('#sip_password');
const id = $('#id');
const uuid = create_UUID();
let ws = null;

export const startApiSettingsSocket = () => {
    ws = websocket(uuid);

    ws.onopen = () => {
        console.log('Connection: OK.');
    };
    ws.onerror = (uuid) => {
        console.clear();
    };
    ws.onmessage = (message) => {
        let obj = message.data;
        try {
            id.prop('disabled', false);
            obj = JSON.parse(obj);
            if (obj.downloaded) {
                if (obj.type === 'audio') {
                    change_dialog_waiting(text_download_audio_success);
                    downloadPlanWithTimer();
                } else if (obj.type === 'plan') {
                    showPlanMessageWithTimer(text_download_plan_success);
                }
            } else if (!obj.downloaded) {
                if (obj.type === 'audio') {
                    change_dialog_waiting(text_download_checksum_not_correct);
                    downloadPlanWithTimer();
                } else {
                    showPlanMessageWithTimer(text_download_checksum_not_correct);
                }
            }
        } catch (exception) {
        }
    }
}

const downloadPlanWithTimer = () => setTimeout(() => {
    change_dialog_waiting(text_download_plan);
    httpGetAsync(getPlan(uuid), null, null);
}, 2000);

const showPlanMessageWithTimer = (message) => setTimeout(() => {
    change_dialog_waiting(message);
    setTimeout( () => {
        dialog_waiting_hide();
    }, 2000);
}, 2000);

export const setDefaultStyleAddress = (input, style) => {
    input.removeClass();
    input.attr('class', 'input_settings ' + style);
}

const setSipServerConfig = (data) => {
    sip_host.val(data.hostname);
    sip_port.val(data.port);
    sip_operator.val(data.operator);
}

const setWebConfig = (data) => {
    web_host.val(data.hostname);
    web_port.val(data.port);
}

const setButton = (data) => {
    button.val(data.id);
}

const setSipAccount = (data) => {
    sip_account_host.val(data.sipRegServer.hostname);
    sip_account_port.val(data.sipRegServer.port);
    account.val(data.account.account);
    sip_password.val(data.account.password);
}

id.click(() => {
    id.prop('disabled', true);
    dialog_waiting_show();
    const web_server_config = {}
    web_server_config.hostname = web_host.val();
    web_server_config.port = web_port.val();
    httpPutAsync(getRegRescue(uuid), setRegRescue, web_server_config);
});

$('#sip_id').click(() => {
    const accWithRegServer = {}
    accWithRegServer.account = {}
    accWithRegServer.account.account = account.val();
    accWithRegServer.account.password = sip_password.val();
    accWithRegServer.regServer = {}
    accWithRegServer.regServer.hostname = sip_account_host.val();
    accWithRegServer.regServer.port = sip_account_port.val();
    httpPutAsync(getRegSip(), setRegSip, accWithRegServer);
});

$('#save_sip_server').click(() => {
    dialog_text.html(save_settings);
    dialog.show();
});

const setRegRescue = (data) => {
    if (data.error) {
        showError(data);
    } else {
        change_dialog_waiting(text_download_audios);
        setTimeout(function () {
            httpGetAsync(getAudios(uuid), downloadStatusAudio, null);
        }, 2000);
        button.val(data.id);
    }
}

const downloadStatusAudio = (data) => {
    if (data.error) {
        showError(data);
    }
}

const setRegSip = (data) => {
    if (data.error) {
        showError(data);
    } else {
        account.val(data.account.account);
        sip_password.val(data.account.password);
    }
}

export const putSipConfig = () => {
    const sip = {};
    sip.hostname = sip_host.val();
    sip.port = sip_port.val();
    sip.operator = sip_operator.val();
    httpPutAsync(setSipConfig(), setSipServerConfig, sip);
}

export const setApiSettings = (data) => {
    setSipServerConfig(data.sip);
    setWebConfig(data.centralServer);
    setSipAccount(data.sip);
    if (data.hasOwnProperty('rescueId')) {
        setButton(data.rescueId);
    }
}

export const closeSocket = () => {
    if(ws !== null){
        ws.close();
    }
}

