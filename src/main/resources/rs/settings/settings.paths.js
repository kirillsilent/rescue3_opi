import {createRequestUri} from "../create.uri.js";

const root = 'settings';
const is_set = 'is_set';

export const isSettingsSet = () => createRequestUri(root, is_set);
