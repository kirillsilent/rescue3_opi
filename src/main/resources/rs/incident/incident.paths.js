import {createRequestUri} from "../create.uri.js";

const root = 'incident_settings';
const get = 'get';
const set = 'set';

export const getIncidentSettings = () => createRequestUri(root, get);
export const setIncidentSettings = () => createRequestUri(root, set);

