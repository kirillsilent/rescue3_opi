import {createRequestUri} from "../create.uri.js";

const root_path = 'wifi';

const get_point = 'get_point';
const get_points = 'get_points';
const get_connection = 'get_connection';
const paramPoint = '?point=';
const connect = 'connect';

export const getPoint = () => createRequestUri(root_path, get_point);

export const getPoints = () => createRequestUri(root_path, get_points);

export const getConnection = point => createRequestUri(root_path, get_connection + paramPoint + point);

export const connectPoint = () => createRequestUri(root_path, connect);