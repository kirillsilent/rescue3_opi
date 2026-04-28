import "../../../../jquery/jquery-3.2.1.min.js"
import { default_style_network, sub_ip } from "../network.js"
import { check_number, check_input_text} from "./input.js";
import { netmask1 } from "./netmask.js"

const ip1 = $('#inp1_ip');
const ip2 = $('#inp2_ip');
const ip3 = $('#inp3_ip');
const ip4 = $('#inp4_ip');

ip1.focusin((id) => default_style_network(sub_ip, id));

ip2.focusin((id) => default_style_network(sub_ip, id));

ip3.focusin((id) => default_style_network(sub_ip, id));

ip4.focusin((id) => default_style_network(sub_ip, id));

ip1.keyup(() => check_input_text(ip1, ip2, null));

ip2.keyup(() => check_input_text(ip2, ip3, ip1));

ip3.keyup(() => check_input_text(ip3, ip4, ip2));

ip4.keyup(() => check_input_text(ip4, netmask1, ip3));

ip1.on('input',() => check_number(ip1));

ip2.on('input',() => check_number(ip2));

ip3.on('input', () => check_number(ip3));

ip4.on('input',() => check_number(ip4));

export { ip4 }