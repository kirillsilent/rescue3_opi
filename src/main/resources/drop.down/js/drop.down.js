import {ioClick} from "../../settings/js/devices/io.js";
import {getErrNotAvailable} from "../../settings/js/devices/io/error.io.strings.js";

const changeHeaderCSS = (header) => {
    if(header.hasClass('active')){
        clearHeaderCSS(header);
    }else {
        header.addClass('active');
    }
}

const clearHeaderCSS = (header) => {
    header.removeClass('active');
}

const createData = (header, body, data, type) => {
    data.forEach((item) => {
        createItem(header, body, item, type);
    });
}


const createItem = (header, body, item) =>{
    const list_item = $('<div class="item drop"></div>');
    const item_text =$('<div class="t i"></div>');
    $(item_text).text(item.name || item.device);
    $(list_item).click(() => {
        changeHeaderCSS(header);
        body.hide();
        body.empty();
        if($(this).text() !== getErrNotAvailable){
            ioClick(item);
        }
    });
    $(list_item).append(item_text);
    body.append(list_item);
}

const hideOpened = (parent, header, body) => {
    $(parent).find('.header.drop').each(function () {
         if(header.attr('id') !==$(this).attr('id')){
            clearHeaderCSS($(this));
         }
    });
    $(parent).find('.cont.drop').each(function () {
         if(body.attr('id') !==$(this).attr('id')){
            $(this).hide();
         }
    });
}

export const drop = (parent, header, body, data, type) => {
    changeHeaderCSS(header);
    body.empty();
    hideOpened(parent, header, body);
    if (!body.is(':visible')) {
        body.show();
    }else {
        body.hide();
    }
    createData(header, body, data, type);
}