import {lan, wifi} from "../constants.js";

export const netInterfaceType = iface => {
    if(iface[0] === 'w'){
        return wifi;
    }else if(iface[0] === 'e') {
        return lan;
    }
};