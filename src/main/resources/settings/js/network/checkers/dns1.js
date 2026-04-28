import "../../../../jquery/jquery-3.2.1.min.js"
import { default_style_network, sub_dns1 } from "../network.js"
import { check_number, check_input_text} from "./input.js";
import { gateway4 } from "./gateway.js";
import { dns2_1 } from "./dns2.js"

export const dns1_1 = $('#inp1_dns1');
const dns1_2 = $('#inp2_dns1');
const dns1_3 = $('#inp3_dns1');
export const dns1_4 = $('#inp4_dns1');

dns1_1.focusin((id) => default_style_network(sub_dns1, id));

dns1_2.focusin((id) => default_style_network(sub_dns1, id));

dns1_3.focusin((id) => default_style_network(sub_dns1, id));

dns1_4.focusin((id) => default_style_network(sub_dns1, id));

dns1_1.keyup(() => check_input_text(dns1_1, dns1_2, gateway4));

dns1_2.keyup(() => check_input_text(dns1_2, dns1_3, dns1_1));

dns1_3.keyup(() => check_input_text(dns1_3, dns1_4, dns1_2));

dns1_4.keyup(() => check_input_text(dns1_4, dns2_1, dns1_3));

dns1_1.on('input',() => check_number(dns1_1));

dns1_2.on('input',() => check_number(dns1_2));

dns1_3.on('input',() => check_number(dns1_3));

dns1_4.on('input',() => check_number(dns1_4));