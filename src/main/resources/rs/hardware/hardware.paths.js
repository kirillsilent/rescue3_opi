import {createRequestUri} from "../create.uri.js";

const root_hardware = 'hardware';
const root_audio = 'audio';
const root_camera = 'camera';

const get = 'get';
const set = 'set';

const get_cards = 'get_cards';
const audio_type_input = '?type=input';
const audio_type_output = '?type=output';

const get_volume = 'get_volume';
const set_volume = 'set_volume';
const volume_param = '?volume=';
const is_available = 'is_available';
const type_param = '?type='

export const getHardware = (hardwareType) => createRequestUri(root_hardware, get + '/' + hardwareType);

export const setHardware = () => createRequestUri(root_hardware, set);

export const getHardwares = () => createRequestUri(root_hardware, get);

export const getInputAudioCards = () => createRequestUri(root_hardware, root_audio
    + '/' + get_cards + audio_type_input);

export const getOutputAudioCards = () => createRequestUri(root_hardware, root_audio
    + '/' + get_cards + audio_type_output);

export const getCameraCards = () => createRequestUri(root_hardware, root_camera
    + '/' + get_cards);

export const getVolume = () => createRequestUri(root_hardware, root_audio + '/' + get_volume);

export const setVolume = (volume) => createRequestUri(root_hardware,
    root_audio + '/' + set_volume + volume_param + volume);

export const audioIsAvailable = (type) => createRequestUri(root_hardware, root_audio + '/' + is_available + type_param + type);
export const cameraIsAvailable = (type) => createRequestUri(root_hardware, root_camera + '/' + is_available + type_param + type);