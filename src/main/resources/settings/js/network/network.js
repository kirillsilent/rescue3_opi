import "./checkers/ip.js"
import "./checkers/netmask.js"
import "./checkers/gateway.js"
import "./checkers/dns1.js"
import {connect, disconnect, lan, wifi} from "./constants.js";
import {httpGetAsync, httpPutAsync} from "../../../rs/requests.js";
import {clearTimer, startTimer} from "../../../utils/timer/timer.interval.js";
import {changeTab, select_network, tab_network, wifi_list} from "../settings.js";
import {dialog_waiting_hide, dialog_waiting_show, showError} from "../../../dialogs/js/dialog.js";
import {getWiFiPoints} from "./wifi.js";
import {netInterfaceType} from "./checkers/net.interface.type.js";
import {getValue} from "../../../utils/storage/storage.js";
import {key_tab} from "../../../utils/storage/keys.js";

import {
    getCurrentNetworkInterface,
    getDefaultRoute,
    getNetworkInterfaces, getWorkOnVPN, setWorkOnVPN
} from "../../../rs/network/network.paths.js";
import {getPoints, getPoint} from "../../../rs/wifi/wifi.paths.js";
import {get_interfaces} from "./select/interface.js";

const net_change = $('#net_change');
const interface_change = $('#interface_change');
const net_status = $('#net_status');
const wifi_change = $('#wifi_change');
const text_ssid = $('.text.ssid');
const network_type = $('#network_type');
const sub_ip = $('#sub_ip');
const sub_netmask = $('#sub_netmask');
const sub_gateway = $('#sub_gateway');
const sub_dns1 = $('#sub_dns1');
const sub_dns2 = $('#sub_dns2');
const vpn_is_enabled = $('#vpn_is_enabled');
const sub_vpn_ip = $('#sub_vpn_ip');
const vpn_status = $('#vpn_status');

let timer_network = null;
let count = 0;
let is_static_set = false;
let first_request = false;
let network_values = {};

export function get_net(data, point) {
    network_values = data;
    disable_focus(sub_vpn_ip, true);
    if (data.error) {
        dialog_waiting_hide();
        showError(data);
    } else {
        if (netInterfaceType(data.iface) === lan) {
            net_change.text(lan);
            wifi_change.hide();
            text_ssid.hide();
            set_net(data);
        } else {
            text_ssid.show();
            if (point !== undefined) {
                set_wifi_name(point);
            } else {
                httpGetAsync(getPoint(), set_wifi_name);
            }
            net_change.text(wifi);
        }
        timer_network = startTimer(timer_network, get_network_current);
    }
}

function set_net(data) {
    interface_change.text(data.iface);
    if (!network_type.is(":checked")) {
        if (data.hasOwnProperty('staticIp')) {
            if (!first_request) {
                first_request = true;
                network_type.prop("checked", data.staticIp);
            }
        }
    }
    if (data.hasOwnProperty('ip')) {
        count = 0;
        httpGetAsync(getDefaultRoute(), is_route_default);
        parse_net(data);
        dialog_waiting_hide();
    } else {
        count++;
        if (!network_type.is(":checked")) {
            clear_network_all();
            parse_net();
        }
        add_style_text(net_status, 'text_status err', disconnect);
        if (count === 4) {
            count = 0;
            dialog_waiting_hide();
        }
    }
}

function parse_net(data) {
    add_style_text(net_status, 'text_status success', connect);
    if (!network_type.is(':checked')) {
        clear_network_all();
        if (data !== undefined) {
            split_network_all(data);
        }
        default_style_network_all();
        disable_focus_all(true);
    } else if (!is_static_set) {
        is_static_set = true;
        clear_network_all();
        split_network_all(data);
        default_style_network_all();
    }
}

function init_static_ip() {
    network_values = {};
    is_static_set = false;
}

function get_network_current() {
    httpGetAsync(getCurrentNetworkInterface(), get_net);
}

function is_network_init() {
    dialog_waiting_hide();
}

function is_route_default(data) {
    dialog_waiting_hide();
}

function set_wifi_name(data) {
    wifi_change.show();
    if (data.ssid === null || data.ssid === '' || Object.keys(data).length === 0 && data.constructor === Object) {
        wifi_change.text(disconnect);
        if (!network_type.is(":checked")) {
            clear_network_all();
        }
    } else {
        wifi_change.text(data.ssid);
        set_net(network_values);
    }
}

function split_network_all(data) {
    split_network_values(data.ip, sub_ip);
    split_network_values(data.netmask, sub_netmask);
    if (data.gateway !== undefined) {
        split_network_values(data.gateway, sub_gateway);
    } else {
        clear_network(sub_gateway);
    }
    if (data.dns1 !== undefined) {
        split_network_values(data.dns1, sub_dns1);
    } else {
        clear_network(sub_dns1);
    }
    if (data.dns2 !== undefined) {
        split_network_values(data.dns2, sub_dns2);
    } else {
        clear_network(sub_dns2);
    }
    if (data.vpn !== undefined) {
        split_network_values(data.vpn, sub_vpn_ip);
        vpn_status.hide();
        sub_vpn_ip.show();
        if (vpn_is_enabled.is(":disabled")) {
            vpn_is_enabled.prop("disabled", false);
        }
    } else {
        sub_vpn_ip.hide();
        vpn_status.show();
        if (vpn_is_enabled.is(":checked")) {
            httpPutAsync(setWorkOnVPN(), set_work_on_vpn, data);
            vpn_is_enabled.prop("disabled", true);
            vpn_is_enabled.prop("checked", false);
        }
        clear_network(sub_vpn_ip);
    }
}

function split_network_values(str, parent) {
    const arr = str.split('.');
    for (let i = 0; i < arr.length; i++) {
        parent.children().each(function () {
            if ($(this).val() === '') {
                $(this).val(arr[i]);
                return false;
            }
        });
    }
}

function default_style_network(parent, id) {
    parent.children('input').each(function () {
        if (this.id === id || id === null) {
            $(this).removeClass();
            $(this).attr('class', 'input_settings net');
        }
    });
}

function disable_focus(parent, is_disabled) {
    parent.children('input').each(function () {
        $(this).prop('disabled', is_disabled);
    });
}

function disable_focus_all(is_disabled) {
    disable_focus(sub_ip, is_disabled);
    disable_focus(sub_netmask, is_disabled);
    disable_focus(sub_gateway, is_disabled);
    disable_focus(sub_dns1, is_disabled);
    disable_focus(sub_dns2, is_disabled);
}

function default_style_network_all() {
    default_style_network(sub_ip, null);
    default_style_network(sub_netmask, null);
    default_style_network(sub_gateway, null);
    default_style_network(sub_dns1, null);
    default_style_network(sub_dns2, null);
}


function add_style_text(html, style, text) {
    html.removeClass();
    html.attr('class', style);
    html.text(text);
}

function clear_network(parent) {
    parent.children('input').each(function () {
        $(this).val('');
    });
}

function get_values(parent) {
    let tmp = '';
    parent.children('input').each(function () {
        if ($(this).val() !== '') {
            tmp += $(this).val() + '.';
        } else {
            if (parent.attr('id') === sub_dns2.attr('id')) {
                return false;
            } else {
                $(this).addClass('exception');
            }
        }
    });
    if (tmp !== '') {
        return tmp.replace(/.$/, '');
    } else {
        return tmp
    }
}

function clear_network_all() {
    clear_network(sub_ip);
    clear_network(sub_netmask);
    clear_network(sub_gateway);
    clear_network(sub_dns1);
    clear_network(sub_dns2);
}

function timerNetworkKill() {
    timer_network = clearTimer(timer_network);
}


export function set_work_on_vpn(data) {
    vpn_is_enabled.prop("checked", data.vpnNetworkEnabled);
}

net_change.click(function () {
    timerNetworkKill();
    changeTab(tab_network, select_network, getValue(key_tab));
});

wifi_change.click(function () {
    timerNetworkKill();
    if (!wifi_list.is(':visible')) {
        dialog_waiting_show();
        console.log("Wifi change: " + getPoints())
        httpGetAsync(getPoints(), getWiFiPoints);
        changeTab(tab_network, wifi_list, getValue(key_tab));
    }
});

interface_change.click(function () {
    timerNetworkKill();
    dialog_waiting_show();
    httpGetAsync(getNetworkInterfaces(net_change.text()), get_interfaces);
});

network_type.change(function () {
    if ($(this).is(':checked')) {
        default_style_network_all();
        disable_focus_all(false);
    } else {
        first_request = true;
        clear_network_all();
        default_style_network_all();
        disable_focus_all(true);
    }
});

vpn_is_enabled.change(function () {
    let data = {};
    data.vpnNetworkEnabled = $(this).is(':checked');
    httpPutAsync(setWorkOnVPN(), set_work_on_vpn, data);
});

export const get_values_network = () => {
    dialog_waiting_show();
    const obj = {};
    obj.iface = interface_change.text();
    obj.staticIp = network_type.is(":checked");
    if (obj.staticIp === true) {
        obj.ip = get_values(sub_ip);
        obj.netmask = get_values(sub_netmask);
        obj.gateway = get_values(sub_gateway);
        obj.dns1 = get_values(sub_dns1);
        obj.dns2 = get_values(sub_dns2);

        if (!validate_network(obj.ip)
            || !validate_network(obj.gateway)
            || !validate_network(obj.netmask)
            || !validate_network(obj.gateway)
            || !validate_network(obj.dns1)) {
            return undefined;
        } else {
            return obj;
        }

    }
    return obj;
};

function save_network() {
    setTimeout(() => {
        init_static_ip();
        httpGetAsync(getCurrentNetworkInterface(), get_net);
    }, 5000);
}

function validate_network(address) {
    return /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(address);
}

function set_first_request() {
    first_request = false;
}

export {
    default_style_network,
    timerNetworkKill,
    save_network,
    default_style_network_all,
    sub_ip,
    sub_netmask,
    sub_gateway,
    sub_dns1,
    sub_dns2,
    init_static_ip,
    set_first_request
}
