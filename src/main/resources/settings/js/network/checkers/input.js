export const check_number = input => {
    input.val(input.val().match(/[0-9]*/));
};

export const check_input_text = (input1, input2, input3) => {
    if(input1.val().length === 3){
        if(parseInt(input1.val()) > 255){
            input1.val(255);
        }
        if(input2 !== null){
            input2.focus();
        }
    }
    input1.keyup(function (e) {
        if(e.keyCode === 8 && input1.val().length === 0){
            if(input3!==null){
                input3.focus();
            }
        }
    });
};

export const check_port = input => {
    if(parseInt(input.val()) > 65535){
        input.val(65535);
    }
};