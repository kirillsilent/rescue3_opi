import {createRequestUri} from "../create.uri.js";

const root = 'marquee';
const get = 'get';

export const getMarquee = () => createRequestUri(root, get);
