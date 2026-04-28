import {getErrAudio, getErrCamera, getErrNotInstalled} from "./error.io.strings.js";
import {setName, disableAudioVolume} from "./set.io.device.js"
import {showErr, hideErr} from "./err.js";
import {getHeaderDrop, getSlider, getValFromSlider} from "./device.js";
import {getTypeAudioInput} from "../../../../rs/hardware/hardware.type.js";
import {setAudio} from "../io.js";

let isAvailableInput = true;
let isAvailableOutput = true;

export const checkAudioCards = (drop, volume, io, err) => {
    const header = $(getHeaderDrop(drop));
    const slider = $(getSlider(volume));
    const vol = $(getValFromSlider(volume));
    if (isIOInstalled(io)) {
        hideErr(err);
        if (header.text() !== io) {
            setName(header, io);
        }
    } else {
        disableAudioVolume(slider, vol);
        setName(header, getErrNotInstalled());
    }
}

export const isIOInstalled = (val) => {
    return val !== undefined;
}

export const checkCamera = (drop, io, err) => {
    const header = $(getHeaderDrop(drop));
    if (isIOInstalled(io)) {
        hideErr(err);
        if (header.text() !== io) {
            setName(header, io);
        }
    } else {
        setName(header, getErrNotInstalled);
    }
}

export const checkAvailableCamera = (io, drop, err) => {
    if (!io.available) {
        showErr(err, getErrCamera());
    } else {
        hideErr(err);
    }
}

export const checkAvailableAudio = (io, drop, volume, err) => {
    const slider = $(getSlider(volume));
    const vol = $(getValFromSlider(volume));
    const header = $(getHeaderDrop(drop));
    if (header.text() !== getErrNotInstalled()) {
        if (!io.available) {
            disableAudioVolume(slider, vol);
            showErr(err, getErrAudio());
            if (io.type === getTypeAudioInput()) {
                isAvailableInput = io.available;
            }else {
                isAvailableOutput = io.available;
            }
        } else {
            io.device = header.text();
            if (io.type === getTypeAudioInput()) {
                if (!isAvailableInput) {
                    setAudio(io);
                    isAvailableInput = io.available;
                }
            } else {
                if (!isAvailableOutput) {
                    setAudio(io);
                    isAvailableOutput = io.available;
                }
            }
            hideErr(err);
        }
    }
}