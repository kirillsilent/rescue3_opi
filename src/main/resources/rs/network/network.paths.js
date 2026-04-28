import {createRequestUri} from "../create.uri.js";

const root = 'network';

const set_interface = 'set_interface';
const set_config = 'set_config';
const get_interfaces = 'get_interfaces'
const get_current_interface = 'get_current_interface';
const get_default_route = 'get_default_route';
const add_default_route = 'add_default_route';
const paramType = '?type=';
const get_work_on_vpn = 'get_work_on_vpn';
const set_work_on_vpn = 'set_work_on_vpn';

export const setNetworkInterface = () => createRequestUri(root, set_interface);

export const setNetworkConfig = () => createRequestUri(root, set_config);

export const getCurrentNetworkInterface = () => createRequestUri(root, get_current_interface);

export const getDefaultRoute = () => createRequestUri(root, get_default_route);

export const addDefaultRoute = () => createRequestUri(root, add_default_route);

export const getNetworkInterfaces = networkType => createRequestUri(root, get_interfaces + paramType + networkType);

export const setWorkOnVPN = () => createRequestUri(root, set_work_on_vpn);

export const getWorkOnVPN = () => createRequestUri(root, get_work_on_vpn);