export const getHeaderDrop = (parent) =>  {
    let header = null;
    parent.find('.header.drop').each(function () {
        header = $(this);
        return false;
    });
    return header;
}

export const getBodyDrop = (parent) => {
    let body = null;
    parent.find('.cont.drop').each(function () {
        body = $(this);
        return false;
    });
    return body;
}

export const getSlider = (parent) => {
    let slider = null;
    parent.find('.sound_slider').each(function () {
        slider = $(this);
        return false;
    });
    return slider;
}

export const getValFromSlider = (parent) => {
    let volume = null;
    parent.find('.val').each(function () {
        volume = $(this);
        return false;
    });
    return volume;
}