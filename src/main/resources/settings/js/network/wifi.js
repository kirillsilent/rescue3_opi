import {
    dialog_waiting_show,
    dialog_waiting_hide,
    dialog_wifi_psk_show,
    dialog_wifi_psk_clear, showError
} from "../../../dialogs/js/dialog.js";
import {clearList} from "../../../utils/ui/clear.list.ui.js";
import {httpGetAsync, httpPostAsync} from "../../../rs/requests.js";
import {changeTab, tab_network, network, select_network, wifi_list} from "../settings.js";
import {get_net, init_static_ip} from "./network.js";
import {back_select_net, update_net} from "./constants.js";
import {getValue} from "../../../utils/storage/storage.js";
import {key_tab} from "../../../utils/storage/keys.js";
import {getCurrentNetworkInterface} from "../../../rs/network/network.paths.js";
import {connectPoint, getPoints, getConnection} from "../../../rs/wifi/wifi.paths.js";

const encrypt_img_url = '../images/wifi_encrypted/';
const free_img_url = '../images/wifi_free/';

const points = $('#wifi_ap');
let point = {};
const wifi_err = $('#wifi_err');
const psk = $('#psk');
let count = 0;
const err_dev = $('#err_dev');
const update_list_ap = $('#update_list_ap');

export const getWiFiPoints = (data) => {
    const points_arr = data.points;
    if (points_arr !== undefined) {
        update_list_ap.text(update_net);
        err_dev.hide();
        count = 0;
        dialog_waiting_hide();
        clearList(points);
        for (let i = 0; i < points_arr.length; i++) {
            if (points_arr[i].signal > -90 && points_arr[i].ssid !== undefined && points_arr[i].ssid !== '') {
                let list_item = $('<li></li>');
                list_item.text(points_arr[i].ssid);
                set_wifi_signal(list_item, points_arr[i].signal, points_arr[i].encrypted);
                list_item.on("click", {value: points_arr[i]}, click);
                points.append(list_item);
            }
        }
    } else {
        count++;
        if (count <= 3) {
            setTimeout(function () {
                httpGetAsync(getPoints(), getWiFiPoints);
            }, 5000);
        } else if (count === 4) {
            count = 0;
            clearList(points);
            update_list_ap.text(back_select_net);
            err_dev.show();
            dialog_waiting_hide();
        }
    }
}

function click(event) {
    wifi_err.hide();
    dialog_wifi_psk_clear();
    point = event.data.value;
    httpGetAsync(getConnection(point.ssid), is_connected);
}

export function is_connected(data) {
    if (data.connected) {
        move_to_settings();
    } else {
        if (point.encrypted) {
            if(wifi_err.is(":visible")){
                wifi_err.hide();
            }
            dialog_wifi_psk_show();
        } else {
            set_wifi();
        }
    }
}


export function set_wifi() {
    dialog_waiting_show();
    const tmpPoint = {};
    tmpPoint.ssid = point.ssid;
    tmpPoint.psk = psk.val();
    httpPostAsync(connectPoint(), is_wifi_associated, tmpPoint);
}

function is_wifi_associated(data) {
    if (data.connected) {
        move_to_settings();
    } else {
        wifi_err.show();
        dialog_waiting_hide();
        dialog_wifi_psk_show();
    }
}

export const is_wifi_ap_set = data => {
    if (data.ssid === null || data.ssid === '' || Object.keys(data).length === 0 && data.constructor === Object) {
        if (!wifi_list.is(':visible')) {
            httpGetAsync(getPoints(), getWiFiPoints);
            changeTab(tab_network, wifi_list, getValue(key_tab));
        } else {
            dialog_waiting_hide();
            changeTab(tab_network, select_network, getValue(key_tab));
        }
    } else {
        init_static_ip();
        httpGetAsync(getCurrentNetworkInterface(), get_net, data);
        changeTab(tab_network, network, getValue(key_tab));
    }
};

function set_wifi_signal(item, signal, encrypted) {
    if (signal > -67) {
        set_wifi_image(item, 4, encrypted);
    } else if (signal > -70) {
        set_wifi_image(item, 3, encrypted);
    } else if (signal > -80) {
        set_wifi_image(item, 2, encrypted);
    } else if (signal > -90) {
        set_wifi_image(item, 1, encrypted);
    }
}

function set_wifi_image(item, strength, encrypted) {
    if (encrypted) {
        item.css('background-image', 'url(../settings/images/' + encrypt_img_url + strength + '.png)');
    } else {
        item.css('background-image', 'url(../images/' + free_img_url + strength + '.png)');
    }
}

update_list_ap.click(function () {
    if (update_list_ap.text() === back_select_net) {
        changeTab(tab_network, select_network, getValue(key_tab));
    } else {
        dialog_waiting_show();
        console.log("Update list AP: " + getPoints())
        httpGetAsync(getPoints(), getWiFiPoints);
    }
});

function move_to_settings() {
    psk.val('');
    wifi_err.hide();
    init_static_ip();
    httpGetAsync(getCurrentNetworkInterface(), get_net)
    changeTab(tab_network, network, getValue(key_tab));
}