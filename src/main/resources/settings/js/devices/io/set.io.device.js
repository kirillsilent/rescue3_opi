export const setName = (header, device) => {
    header.text(device);
}

const setAudioVolume = (slider, volume, val) => {
    slider.val(val);
    volume.text(val);
}

export const enableAudioVolume = (slider, volume, val) => {
    if(slider.is(':disabled')){
        slider.prop('disabled', false);
    }
    setAudioVolume(slider, volume, val);
}

export const disableAudioVolume = (slider, volume) => {
    if(slider.is(':enabled')){
        slider.prop('disabled', true);
    }
    setAudioVolume(slider, volume, '0');
}