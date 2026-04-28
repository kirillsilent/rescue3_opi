import "../../../../jquery/jquery-3.2.1.min.js"
import { default_style_network, sub_gateway } from "../network.js"
import { check_number, check_input_text} from "./input.js";
import { netmask4 } from "./netmask.js"
import { dns1_1 } from "./dns1.js"

const gateway1 = $('#inp1_gateway');
const gateway2 = $('#inp2_gateway');
const gateway3 = $('#inp3_gateway');
const gateway4 = $('#inp4_gateway');

gateway1.focusin((id) => default_style_network(sub_gateway, id));

gateway2.focusin((id) => default_style_network(sub_gateway, id));

gateway3.focusin((id) => default_style_network(sub_gateway, id));

gateway4.focusin((id) => default_style_network(sub_gateway, id));

gateway1.keyup(() => check_input_text(gateway1, gateway2, netmask4));

gateway2.keyup(() => check_input_text(gateway2, gateway3, gateway1));

gateway3.keyup(() => check_input_text(gateway3, gateway4, gateway2));

gateway4.keyup(() => check_input_text(gateway4, dns1_1, gateway3));

gateway1.on('input',() => check_number(gateway1));

gateway2.on('input',() => check_number(gateway2));

gateway3.on('input',() => check_number(gateway3));

gateway4.on('input',() => check_number(gateway4));

export { gateway1, gateway4 }