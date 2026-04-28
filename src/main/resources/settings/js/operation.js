import {
    network,
    api_settings,
    io,
    plan_settings,
    coords_settings,
    tab_network,
    tab_api_settings,
    tab_io,
    select_network
} from "./settings.js";
import {save_settings, text_err_settings_is_not_set} from "../../dialogs/js/dialog.strings.js";
import {dialog, dialog_text, init_dialog_default, showError} from "../../dialogs/js/dialog.js";
import {getValue} from "../../utils/storage/storage.js";
import {previous_tab} from "../../utils/storage/keys.js";
import {httpGetAsync, move} from "../../rs/requests.js";
import {timerIOKill} from "./devices/io.js";
import {timerNetworkKill} from "./network/network.js";
import {isSettingsSet} from "../../rs/settings/settings.paths.js";
//import {getRoot} from "../../views.paths/paths.js";
import {restartSipService} from "../../rs/sip/sip.paths.js";
import {closeSocket} from "./api_settings/api.js";
import {getBackRoot, getRoot} from "../../views.paths/paths.js";
import {settingsAuthLogout} from "../../utils/settings_auth/settings_auth.js";

const save = $('#save');

save.click(function () {
    httpGetAsync(restartSipService(), null, null);
    timerNetworkKill();
    dialog_text.html(save_settings);
    dialog.show();
});

$('#set_default').click(function () {
    init_dialog_default();
});

$('#back').click(() => {
    closeSocket();
    if(select_network.is(':visible')){
        settingsNotSet();
    }else if(network.is(':visible')){
        httpGetAsync(isSettingsSet(), moveToPage);
    }else if(api_settings.is(':visible')){
        tab_network.click();
    }else if(io.is(':visible')){
        tab_api_settings.click();
    }else if(plan_settings && plan_settings.is(':visible')){
        // На вкладке «План» возвращаемся на предыдущую вкладку (какая была перед Планом)
        const prev = getValue(previous_tab);
        if (prev) {
            $('#'+prev).click();
        } else {
            tab_network.click();
        }
    }else if(coords_settings && coords_settings.is(':visible')){
        // На вкладке «Координаты» возвращаемся на предыдущую вкладку
        const prev = getValue(previous_tab);
        if (prev) {
            $('#'+prev).click();
        } else {
            tab_network.click();
        }
    }else {
        // Fallback: если появятся новые вкладки — возвращаемся на previous_tab если есть
        const prev = getValue(previous_tab);
        if (prev) {
            if (prev === tab_io.attr('id')) {
                timerIOKill();
            }
            $('#'+prev).click();
        } else {
            tab_network.click();
        }
    }
});

const moveToPage = (data) => {
    if(data.set){
            //httpGetAsync(restartSipService(), null, null);
            settingsAuthLogout().finally(() => move(getBackRoot()));
    }else {
        settingsNotSet();
    }
}

const settingsNotSet = () => {
    //httpGetAsync(restartSipService(), null, null);
    settingsAuthLogout().finally(() => move(getBackRoot()));
}

export const saveHide = () => {
    save.hide();
};

export const saveShow = () => {
    save.show()
};
