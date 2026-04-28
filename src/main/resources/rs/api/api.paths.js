import {createRequestUri} from "../create.uri.js";

const root = 'settings_api';
const get ='get';
const get_reg_rescue = 'get_reg_rescue';
const get_reg_sip = 'get_reg_sip';
const get_audios = 'get_audios';
const get_plan = 'get_plan';
const set_sip_config = 'set_sip_config';
const set_event_server = 'set_event_server';
const param_uuid = '?uuid='

export const getApiSettings = () => createRequestUri(root, get);

export const getRegRescue = () => createRequestUri(root, get_reg_rescue);

export const getRegSip = () => createRequestUri(root, get_reg_sip);

export const getAudios = (uuid) => createRequestUri(root, get_audios + param_uuid +uuid);

export const getPlan = (uuid) => createRequestUri(root, get_plan + param_uuid + uuid);

export const setSipConfig = () => createRequestUri(root, set_sip_config);

export const setEventServerUrl = () => createRequestUri(root, set_event_server);
