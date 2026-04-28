import {createRequestUri} from "../create.uri.js";

const root_sip = 'sip_client';
const root_api = 'api';
const root_network = 'network';

const status = 'status';

export const getStatusSip = () => createRequestUri(root_sip, status);
export const getStatusAPI = () => createRequestUri(root_api, status);
export const getStatusNetwork = () => createRequestUri(root_network, status);