import "../../jquery/jquery-3.2.1.min.js"
import {
    default_id, default_network, default_server, default_all, text_action_default_id, text_action_default_network,
    text_action_default_settings, text_action_default_server, text_inputs_err, text_reg_err, waiting
} from "./dialog.strings.js";
import {network, api_settings, coords_settings} from "../../settings/js/settings.js";
import {div_network, div_settings, div_reset_id, div_server} from "./dialog.default.child.js";
import {set_wifi} from "../../settings/js/network/wifi.js";
import {get_values_network, save_network} from "../../settings/js/network/network.js"
import {httpPutAsync} from "../../rs/requests.js";
import {putSipConfig} from "../../settings/js/api_settings/api.js";
import {putEventServerUrlFromCoords, putIncidentSettingsUI} from "../../settings/js/coords/coords.js";
import {setNetworkConfig} from "../../rs/network/network.paths.js";

const dialog = $('#dialog');
const dialog_text = $('#dialog_text');
const dialog_notification = $('#dialog_notification');
const dialog_text_notification = $('#dialog_text_notification');
const dialog_default = $('#dialog_default');
const dialog_waiting = $('#dialog_waiting');
const dialog_waiting_text = $('#waiting');
const dialog_wifi_psk = $('#dialog_wifi_psk');
const default_content = $('#default_content');
const input_psk = $('#psk');
let dialog_action = null;


$('#ok').click(function () {
    if (dialog.is(':visible')) {
        if (network.is(':visible')) {
            const net = get_values_network();
            if (net === undefined) {
                dialog_waiting_hide();
                dialog_notification.show();
                dialog_text_notification.html(text_inputs_err);
            } else {
                dialog.hide();
                dialog_waiting_show();
                httpPutAsync(setNetworkConfig(), save_network, net)
            }
        } else if (api_settings.is(':visible')) {
            dialog.hide();
            putSipConfig();
        } else if (coords_settings.is(':visible')) {
            dialog.hide();
            dialog_waiting_show();
            // сначала сохраняем адрес сервера событий, затем incident-шаблон
            putEventServerUrlFromCoords(() => putIncidentSettingsUI(dialog_waiting_hide));
        }
    }
});

$('#cancel').click(function () {
    if (dialog_action === null) {
        dialog.hide();
    }
});

$('#reset_id').click(function () {
    dialog_default.hide();
    dialog_text.html(default_id);
    dialog.show();
    console.log("Reset id");
});

function click_default_button() {
    dialog_default_hide_action(default_all);
}

function click_default_network() {
    dialog_default_hide_action(default_network);
}

function click_default_id() {
    dialog_default_hide_action(default_id);
}

function click_default_server() {
    dialog_default_hide_action(default_server);
}

$('#close').click(function () {
    dialog_notification.hide();
});

$('#default_cancel').click(function () {
    dialog_default.hide();
});

$('#save_wifi').click(function () {
    if (dialog_wifi_psk.is(':visible')) {
        set_wifi();
    }
});

$('#cancel_wifi').click(function () {
    dialog_wifi_psk.hide();
});

function init_dialog_default() {
    default_content.empty();
    add_child_default_dialog(div_settings, text_action_default_settings, click_default_button);
    if (network.is(':visible')) {
        add_child_default_dialog(div_network, text_action_default_network, click_default_network);
        console.log('network is visible');
    } else if (api_settings.is(':visible')) {
        add_child_default_dialog(div_server, text_action_default_server, click_default_server);
        add_child_default_dialog(div_reset_id, text_action_default_id, click_default_id);
    }
    dialog_default.show();
}

function dialog_default_hide_action(text) {
    dialog_default.hide();
    dialog_text.html(text);
    dialog.show();
}

function add_child_default_dialog(div, text, click) {
    let content = div;
    content.html(text);
    default_content.prepend(content);
    content.click(click);
}

function dialog_waiting_show() {
    if(!dialog_waiting_is_showing()){
        if (dialog_wifi_psk_is_showing()) {
            dialog_wifi_psk.hide();
        }
        dialog_waiting.show();
    }
}

function dialog_waiting_is_showing(){
    return dialog_waiting.is(':visible');
}

function dialog_wifi_psk_is_showing(){
    return dialog_wifi_psk.is(':visible');
}

function change_dialog_waiting(text){
    if (dialog_waiting.is(':visible')) {
        dialog_waiting_text.text(text);
    }
}

function dialog_waiting_hide() {
    if (dialog_waiting.is(':visible')) {
        dialog_waiting_text.text(waiting);
        dialog_waiting.hide();
    }
}

function dialog_wifi_psk_show() {
    dialog_wifi_psk.show()
}

function dialog_wifi_psk_clear() {
    input_psk.val('');
}

function showError(data){
    dialog_notification.show();
    if(data.hasOwnProperty('description')){
        dialog_text_notification.html(data.description);
    } else if (data.hasOwnProperty('error') && data.error) {
        dialog_text_notification.html(data.error);
    }else {
        dialog_text_notification.html(text_reg_err);
    }
}

export {
    dialog,
    dialog_text,
    init_dialog_default,
    dialog_waiting_show,
    dialog_waiting_hide,
    dialog_wifi_psk_show,
    dialog_wifi_psk_clear,
    change_dialog_waiting,
    showError
}
