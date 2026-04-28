import "../../../../jquery/jquery-3.2.1.min.js"
import { default_style_network, sub_netmask } from "../network.js"
import { check_number, check_input_text} from "./input.js";
import { ip4 } from "./ip.js"
import { gateway1 } from "./gateway.js"

const netmask1 = $('#inp1_netmask');
const netmask2 = $('#inp2_netmask');
const netmask3 = $('#inp3_netmask');
const netmask4 = $('#inp4_netmask');

netmask1.focusin((id) => default_style_network(sub_netmask, id));

netmask2.focusin((id) => default_style_network(sub_netmask, id));

netmask3.focusin((id) => default_style_network(sub_netmask, id));

netmask4.focusin((id) => default_style_network(sub_netmask, id));

netmask1.keyup(() => check_input_text(netmask1, netmask2, ip4));

netmask2.keyup(() => check_input_text(netmask2, netmask3, netmask1));

netmask3.keyup(() => check_input_text(netmask3, netmask4, netmask2));

netmask4.keyup(() => check_input_text(netmask4, gateway1, netmask3));

netmask1.on('input',() => check_number(netmask1));

netmask2.on('input', () => check_number(netmask2));

netmask3.on('input',() => check_number(netmask3));

netmask4.on('input',() => check_number(netmask4));

export { netmask1, netmask4 }