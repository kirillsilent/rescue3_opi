export const showErr = (err, text) => {
    if(!err.is(':visible')) {
        err.text(text);
        err.show();
    }
}

export const hideErr = (err) => {
    if(err.is(':visible')){
        err.hide();
    }
}