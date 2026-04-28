import "../../../../jquery/jquery-3.2.1.min.js"
import {dialog_waiting_hide, dialog_waiting_show} from "../../../../dialogs/js/dialog.js";
import {httpGetAsync, httpPutAsync} from "../../../../rs/requests.js";
import {setNetworkInterface} from "../../../../rs/network/network.paths.js";
import {lan_eth0, wlan_wlan0, net_default, lan, update_net} from "../constants.js";
import {changeTab, tab_network, network, select_interfaces} from "../../settings.js";
import {init_static_ip, get_net} from "../network.js";
import {getWiFiPoints} from "../wifi.js";
import {getValue} from "../../../../utils/storage/storage.js";
import {key_tab} from "../../../../utils/storage/keys.js";
import {getNetworkInterfaces} from "../../../../rs/network/network.paths.js";
import {getPoints} from "../../../../rs/wifi/wifi.paths.js";
import {clearList} from "../../../../utils/ui/clear.list.ui.js";


const interfaces = $('#interfaces');
const err_net_interfaces = $('#err_net_interfaces');
const update_list_interfaces = $('#update_list_interfaces');
let type;

function click(event) {
    dialog_waiting_show();
    const iface = {}
    iface.type = 'network';
    iface.device = event.data.value;
    httpPutAsync(setNetworkInterface(), set_interface, iface);
}

function set_interface(data) {
    init_static_ip();
    if (type === lan) {
        changeTab(tab_network, network, getValue(key_tab));
        get_net(data);
    } else {
        httpGetAsync(getPoints(), getWiFiPoints);
    }
}

update_list_interfaces.click(function () {
    dialog_waiting_show();
    httpGetAsync(getNetworkInterfaces(type), get_interfaces);
});

export const get_interfaces = (data) => {
    dialog_waiting_hide();
    changeTab(tab_network, select_interfaces, getValue(key_tab));
    type = data.type;
    if (data.hasOwnProperty('interfaces')) {
        const interfaces_arr = data.interfaces;
        update_list_interfaces.text(update_net);
        err_net_interfaces.hide();
        clearList(interfaces);
        for (let i = 0; i < interfaces_arr.length; i++) {
            const iface = $('<li></li>');
            if (interfaces_arr[i] === lan_eth0 || interfaces_arr[i] === wlan_wlan0) {
                iface.text(interfaces_arr[i] + net_default);
            } else {
                iface.text(interfaces_arr[i]);
            }
            iface.on("click", {value: interfaces_arr[i]}, click);
            interfaces.append(iface);
        }
    } else {
        clearList(interfaces);
        update_list_interfaces.text(update_net);
        err_net_interfaces.show();
    }
};
