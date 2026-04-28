import {createRequestUri} from "../create.uri.js";

const root = 'sip_client';
const status = 'status';
const stop = 'stop';
const restart = 'restart';

export const getStatus = () => createRequestUri(root, status);
export const stopSipService = () => createRequestUri(root, stop);
export const restartSipService = () => createRequestUri(root, restart);