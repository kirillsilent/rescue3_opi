import {drop} from "../../../drop.down/js/drop.down.js"
import {httpGetAsync, httpPostAsync, httpPutAsync} from "../../../rs/requests.js";
import {startTimer, clearTimer} from "../../../utils/timer/timer.interval.js";
import {checkAudioCards, checkAvailableAudio, checkAvailableCamera, checkCamera} from "./io/check.js"
import {getHeaderDrop, getBodyDrop, getSlider, getValFromSlider} from "./io/device.js";

import {
    audioIsAvailable, cameraIsAvailable,
    getCameraCards,
    getHardwares,
    getInputAudioCards,
    getOutputAudioCards, getVolume, setHardware, setVolume
} from "../../../rs/hardware/hardware.paths.js";
import {getTypeAudioInput, getTypeAudioOutput, getTypeCamera} from "../../../rs/hardware/hardware.type.js";
import {enableAudioVolume} from "./io/set.io.device.js";
import {getErrNotAvailable} from "./io/error.io.strings.js";

const parent = $('#func_io');
const input_audio_cards = $('#input_audio_cards');
const output_audio_cards = $('#output_audio_cards');
const volume_input = $('#volume_input');
const volume_output = $('#volume_output');
const camera = $('#camera');
const err_in = $('#err_in');
const err_out = $('#err_out');
const err_camera = $('#err_camera');

let timer_io = null;
const DEFAULT_AUDIO_VOLUME = 90;

const getUiVolume = (val) => {
    const parsed = Number(val);
    return Number.isFinite(parsed) ? parsed : DEFAULT_AUDIO_VOLUME;
}

export const setAudio = (io) => {
    if(io.type === 'input'){
        checkAudioCards(input_audio_cards, volume_input, io.device, err_in);
        if (io.device !== undefined) {
            httpPostAsync(getVolume(), setVolumeInput, io);
        }
    }else {
        checkAudioCards(output_audio_cards, volume_output, io.device, err_out);
        if (io.device !== undefined) {
            httpPostAsync(getVolume(), setVolumeOutput, io);
        }
    }
}

const setVolumeInput = (io) => {
    enableAudioVolume($(getSlider(volume_input)), $(getValFromSlider(volume_input)), getUiVolume(io.volume));
}

const setVolumeOutput = (io) => {
    enableAudioVolume($(getSlider(volume_output)), $(getValFromSlider(volume_output)), getUiVolume(io.volume));
}

$(getHeaderDrop(input_audio_cards)).click(() => httpGetAsync(getInputAudioCards(), getInputCards));

function getInputCards(data) {
    drop(parent, $(getHeaderDrop(input_audio_cards)), $(getBodyDrop(input_audio_cards)), deviceIsNotAvailable(data), getTypeAudioInput());
}

$(getHeaderDrop(output_audio_cards)).click(() => httpGetAsync(getOutputAudioCards(), getOutputCards));

function getOutputCards(data) {
    drop(parent, $(getHeaderDrop(output_audio_cards)), $(getBodyDrop(output_audio_cards)),
        deviceIsNotAvailable(data), getTypeAudioOutput());
}

$(getHeaderDrop(camera)).click(() => httpGetAsync(getCameraCards(), getCameras));

function getCameras(data) {
    drop(parent, $(getHeaderDrop(camera)), $(getBodyDrop(camera)),
        deviceIsNotAvailable(data), getTypeCamera());
}

export const ioClick = (device) => httpPutAsync(setHardware(), setIO, device);

const setIO = (io) => {
    if (io.type === getTypeCamera()) {
        checkCamera(camera, io.device, err_camera);
    }else {
        setAudio(io);
    }
}

$(getSlider(volume_input)).on("input", function () {
    const item = createItemFromDropDown($(getHeaderDrop(input_audio_cards)).text(), getTypeAudioInput());
    httpPutAsync(setVolume($(this).val()), null, item);
    $(getValFromSlider(volume_input)).text($(this).val());
});

$(getSlider(volume_output)).on("input", function () {
    const item = createItemFromDropDown($(getHeaderDrop(output_audio_cards)).text(), getTypeAudioOutput());
    httpPutAsync(setVolume($(this).val()), null, item);
    $(getValFromSlider(volume_output)).text($(this).val());
});

const createItemFromDropDown = (name, type) => {
    const item = {};
    item.device = name;
    item.type = type;
    return item;
}

export const getIOCurrent = () => httpGetAsync(getHardwares(), setCurrentIO);

const setCurrentIO = (data) => {
    const ios = [];
    data.forEach((io) => {
        if (io.type === getTypeAudioInput()) {
            ios.push(io);
            setAudio(io);
        } else if (io.type === getTypeAudioOutput()) {
            ios.push(io);
            setAudio(io);
        }else if (io.type === getTypeCamera()) {
            ios.push(io);
            checkCamera(camera, io.device, err_camera);
        }
    });
    timer_io = startTimer(timer_io, ioIsAvailable, ios);
}

const ioIsAvailable = ios => {
    ios.forEach((io) => {
        if (io.type === getTypeCamera()) {
            httpGetAsync(cameraIsAvailable(io.type), updateUIAvailable, io);
        } else {
            httpGetAsync(audioIsAvailable(io.type), updateUIAvailable, io);
        }
    });
}

const updateUIAvailable = (io) => {
    if (io.type === getTypeCamera()) {
        checkAvailableCamera(io, camera, err_camera);
    } else if (io.type === getTypeAudioInput()) {
        checkAvailableAudio(io, input_audio_cards, volume_input, err_in);
    } else if (io.type === getTypeAudioOutput()) {
        checkAvailableAudio(io, output_audio_cards, volume_output, err_out);
    }

}

const deviceIsNotAvailable = (data) => {
    if (data.length === 0) {
        let obj = {};
        obj.device = getErrNotAvailable();
        obj.volume = null;
        data.push(obj);
    }
    return data;
}

export const timerIOKill = () => {
    timer_io = clearTimer(timer_io);
}
