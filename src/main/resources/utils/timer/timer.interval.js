export const startTimer = (timer, func, data) => {
    if(timer === null){
           timer = setInterval(function () {
               if(data === null){
                   func();
               }else {
                   func(data);
               }

        }, 5000);
    }
    return timer;
}

export const clearTimer = (timer) => {
    if(timer!==null){
        clearInterval(timer);
    }
    return null;
}