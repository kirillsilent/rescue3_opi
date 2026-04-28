import {check_number, check_port} from "../network/checkers/input.js";
import {setDefaultStyleAddress} from "./api.js";

const web_host = $('#web_host');
const web_port = $('#web_port');
const sip_host = $('#sip_host');
const sip_port = $('#sip_port');
const sip_account_host =$('#sip_account_host');
const sip_account_port =$('#sip_account_port');
const button = $('#button');
const sip_operator = $('#sip_operator');

web_host.focusin(function () {
    setDefaultStyleAddress(web_host, 'server');
});

web_port.focusin(function () {
    setDefaultStyleAddress(web_port, 'protocol_port');
});

sip_account_host.focusin(function () {
    setDefaultStyleAddress(sip_account_host, 'server');
});

sip_account_port.focusin(function () {
    setDefaultStyleAddress(sip_account_port, 'protocol_port');
});


sip_host.focusin(function () {
    setDefaultStyleAddress(sip_host, 'server');
});

sip_port.focusin(function () {
    setDefaultStyleAddress(sip_port, 'sip_port');
});

button.focusin(function () {
    setDefaultStyleAddress(button, 'id');
});

sip_operator.focusin(function () {
    setDefaultStyleAddress(sip_operator, 'operator');
});

web_port.on('input',function () {
    check_number($(this));
});

sip_account_port.on('input',function () {
    check_number($(this));
});

sip_port.on('input',function () {
    check_number($(this));
});

button.on('input',function () {
    check_number($(this));
});

sip_operator.on('input',function () {
    check_number($(this));
});

web_port.keyup(function () {
   check_port(web_port);
});

sip_port.keyup(function () {
   check_port(sip_port);
});

sip_account_port.keyup(function () {
    check_port(sip_account_port);
});

export {web_host, web_port, sip_host, sip_port, sip_account_host, sip_account_port, button, sip_operator}