import "../../../../jquery/jquery-3.2.1.min.js"
import { default_style_network, sub_dns2 } from "../network.js"
import { check_number, check_input_text} from "./input.js";
import {dns1_4} from "./dns1.js";

export const dns2_1 = $('#inp1_dns2');
const dns2_2 = $('#inp2_dns2');
const dns2_3 = $('#inp3_dns2');
const dns2_4 = $('#inp4_dns2');

dns2_1.focusin((id) => default_style_network(sub_dns2, id));

dns2_2.focusin((id) => default_style_network(sub_dns2, id));

dns2_3.focusin((id) => default_style_network(sub_dns2, id));

dns2_4.focusin((id) => default_style_network(sub_dns2, id));

dns2_1.keyup(() => check_input_text(dns2_1, dns2_2, dns1_4));

dns2_2.keyup(() => check_input_text(dns2_2, dns2_3, dns2_1));

dns2_3.keyup(() => check_input_text(dns2_3, dns2_4, dns2_2));

dns2_4.keyup(() => check_input_text(dns2_4, null, dns2_3));

dns2_1.on('input',() => check_number(dns2_1));

dns2_2.on('input',() => check_number(dns2_2));

dns2_3.on('input',() => check_number(dns2_3));

dns2_4.on('input',() => check_number(dns2_4));