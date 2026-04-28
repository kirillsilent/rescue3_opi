import {httpGetAsync} from "../../../../rs/requests.js";
import {lan, wifi} from "../constants.js";
import {dialog_waiting_show} from "../../../../dialogs/js/dialog.js";
import {get_interfaces} from "./interface.js";
import {getNetworkInterfaces} from "../../../../rs/network/network.paths.js";


$('#select_lan').click(function () {
    console.log("Select LAN");
    dialog_waiting_show();
    httpGetAsync(getNetworkInterfaces(lan), get_interfaces, true);
});

$('#select_wifi').click(function () {
    dialog_waiting_show();
    console.log("Select WIFI");
    httpGetAsync(getNetworkInterfaces(wifi), get_interfaces, true);
});