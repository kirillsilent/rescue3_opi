import "../../jquery/jquery-3.2.1.min.js"
import "../js/network/select/network.js"
import {httpGetAsync} from "../../rs/requests.js";
import {getHardware} from "../../rs/hardware/hardware.paths.js";
import {getTypeNetwork} from "../../rs/hardware/hardware.type.js";
import {timerNetworkKill, get_net, init_static_ip, set_first_request, set_work_on_vpn} from "./network/network.js"
import {setApiSettings, startApiSettingsSocket, closeSocket } from "./api_settings/api.js"
import {saveShow, saveHide} from "./operation.js"
import {setLocalStorage, getValue} from "../../utils/storage/storage.js";
import {key_tab, previous_tab} from "../../utils/storage/keys.js";
import {timerIOKill, getIOCurrent} from "./devices/io.js";
import {wifi} from "./network/constants.js";
import {dialog_waiting_hide} from "../../dialogs/js/dialog.js";
import {netInterfaceType} from "./network/checkers/net.interface.type.js";
import {is_wifi_ap_set} from "./network/wifi.js";
import {getCurrentNetworkInterface, getWorkOnVPN} from "../../rs/network/network.paths.js";
import {getPoint} from "../../rs/wifi/wifi.paths.js";
import {getApiSettings} from "../../rs/api/api.paths.js";
import {loadIncidentSettingsUI, stopIncidentDateTimeAutoUpdate} from "./coords/coords.js";

let tab;
export const network = $("#network");
export const select_network = $("#select_network");
export const select_interfaces = $("#net_interfaces_list");
export const wifi_list = $("#wifi_list");
export const api_settings = $("#api_settings");
export const io = $("#io");

// ▼ новая вкладка «План»
export const plan_settings = $("#plan_settings");
export const tab_plan = $('#tab_plan');

// ▼ новая вкладка «Координаты»
export const coords_settings = $("#coords_settings");
export const tab_coords = $('#tab_coords');

export const tab_network = $('#tab_network');
export const tab_api_settings = $('#tab_api_settings');
export const tab_io = $('#tab_io');

$(document).ready(() => {
    // Auth is enforced by backend filter for /settings and protected APIs.
    // Avoid client-side reauth loops in kiosk browsers.
    if (getValue(key_tab) === null || tab_network.attr('id') === getValue(key_tab)) {
        tab_network.click();
    } else if (getValue(key_tab) === tab_api_settings.attr('id')) {
        tab_api_settings.click();
    } else if (getValue(key_tab) === tab_io.attr('id')) {
        tab_io.click();
    } else if (tab_plan.length && getValue(key_tab) === tab_plan.attr('id')) {
        tab_plan.click();
    } else if (tab_coords.length && getValue(key_tab) === tab_coords.attr('id')) {
        tab_coords.click();
    } else {
        tab = tab_io;
        changeCssTab(tab_io, true);
    }
});

export const changeCssTab = (tab, change) => {
    if (tab === undefined) {
        return;
    }
    tab.removeClass();
    if (change === true) {
        tab.attr('class', 'tab tab_bottom_bar_selected tab_background_selected tab_padding');
    } else {
        tab.attr('class', 'tab tab_bottom_bar tab_background tab_padding')
    }
}

tab_network.click(function () {
    stopIncidentDateTimeAutoUpdate();
    closeSocket();
    timerNetworkKill();
    timerIOKill();
    set_first_request();
    httpGetAsync(getHardware(getTypeNetwork()), isNetworkInit);
    httpGetAsync(getWorkOnVPN(), set_work_on_vpn);
});

const isNetworkInit = (data) => {
    if (!data.hasOwnProperty('device')) {
        if (!select_network.is(':visible')) {
            dialog_waiting_hide();
            saveHide();
            changeTab(tab_network, select_network, getValue(key_tab));
        }
    } else {
        if (netInterfaceType(data.device) === wifi) {
            saveHide();
            httpGetAsync(getPoint(), is_wifi_ap_set)
        } else {
            if (!network.is(':visible')) {
                changeTab(tab_network, network, getValue(key_tab));
                init_static_ip();
                httpGetAsync(getCurrentNetworkInterface(), get_net);
            }
        }
    }
}

tab_api_settings.click(function () {
    stopIncidentDateTimeAutoUpdate();
    closeSocket();
    timerNetworkKill();
    timerIOKill();
    startApiSettingsSocket();
    if (!api_settings.is(':visible')) {
        httpGetAsync(getApiSettings(), setApiSettings);
        changeTab($(this), api_settings, getValue(key_tab));
        saveHide();
    }
});

tab_io.click(function () {
    stopIncidentDateTimeAutoUpdate();
    closeSocket();
    timerNetworkKill();
    timerIOKill();
    if (!io.is(':visible')) {
        getIOCurrent();
        changeTab($(this), io, getValue(key_tab));
        saveHide();
    }
});

// ▼ обработчик вкладки «План»
tab_plan.click(function () {
    stopIncidentDateTimeAutoUpdate();
    closeSocket();
    timerNetworkKill();
    timerIOKill();
    if (!plan_settings.is(':visible')) {
        changeTab($(this), plan_settings, getValue(key_tab));
        saveHide();          // у «Плана» своя кнопка загрузки
        loadCurrentPlan();   // подтянуть текущее превью
        bindPlanUpload();    // навесить загрузчик (один раз)
    }
});

// ▼ обработчик вкладки «Координаты»
tab_coords.click(function () {
    closeSocket();
    timerNetworkKill();
    timerIOKill();
    if (!coords_settings.is(':visible')) {
        changeTab($(this), coords_settings, getValue(key_tab));
        saveShow();
        loadIncidentSettingsUI();
    }
});

export function changeTab (new_tab, view, previous) {
    setLocalStorage(previous_tab, previous);
    setLocalStorage(key_tab, new_tab.attr('id'));
    changeCssTab(tab, false);
    tab = new_tab;
    changeCssTab(tab, true);
    hideAll();
    if (view === select_network
        || view === wifi_list
        || view === select_interfaces) {
        saveHide();
    } else {
        // Для планов скрываем кнопку «Сохранить» выше по коду в обработчике
        if (view.attr('id') !== 'plan_settings') {
            saveShow();
        }
    }
    view.show();
}

const hideAll = () => $(".container").css({"display":"none"});

// ====== ЛОГИКА «ПЛАН» ======

let planUploadBound = false;

// GET /api/plan -> { url: "...", version: 1730610000000 } или 204
async function loadCurrentPlan() {
    const img = $('#plan_preview_img');
    const video = $('#plan_preview_video');
    const placeholder = $('#plan_preview_placeholder');
    const err = $('#err_plan_preview');

    const hideAll = () => {
        img.hide().removeAttr('src');
        video.hide().removeAttr('src');
        placeholder.hide();
    };

    const showPlaceholder = (text) => {
        hideAll();
        placeholder.text(text || 'План не загружен').show();
    };

    const isVideoUrl = (url) => {
        if (!url) return false;
        return /\.(mp4|webm|ogv|ogg)(\?|#|$)/i.test(url);
    };

    err.text('');
    try {
        const r = await fetch('/api/plan', {
            method: 'GET',
            credentials: 'same-origin',
            cache: 'no-store',
        });

        if (r.status === 204) {
            showPlaceholder('План не загружен');
            return;
        }
        if (!r.ok) {
            showPlaceholder('План не загружен');
            err.text('Не удалось загрузить превью');
            return;
        }

        const data = await r.json();
        if (!data || !data.url) {
            showPlaceholder('План не загружен');
            return;
        }

        const v = data.version ?? Date.now();
        const src = `${data.url}?v=${v}`;
        const type = String(data.type || '').toLowerCase();
        const isVideo = type.startsWith('video/') || isVideoUrl(data.url);

        hideAll();
        if (isVideo) {
            video.attr('src', src).show();
            video.get(0)?.load?.();
        } else {
            img.attr('src', src).show();
        }
    } catch (e) {
        showPlaceholder('План не загружен');
        err.text('Не удалось загрузить превью');
    }
}

// POST /api/plan multipart(file) -> { url, version }
function bindPlanUpload() {
    if (planUploadBound) return;
    planUploadBound = true;

$('#upload_plan').on('click', async () => {
  const fileInput = $('#plan_file')[0];
  const err = $('#err_plan_upload');
  err.text('');

  const file = fileInput?.files?.[0];
  if (!file) { err.text('Выберите файл'); return; }

  // Иногда file.type пустой. Делаем fallback по расширению.
  const allowed = [
    'image/png','image/jpeg','image/webp','image/svg+xml',
    'video/mp4','video/webm','video/ogg'
  ];
  const ext = (file.name.split('.').pop() || '').toLowerCase();
  const mime = file.type || (
    ext === 'png'  ? 'image/png'  :
    (ext === 'jpg' || ext === 'jpeg') ? 'image/jpeg' :
    ext === 'webp' ? 'image/webp' :
    ext === 'svg'  ? 'image/svg+xml' :
    ext === 'mp4'  ? 'video/mp4' :
    ext === 'webm' ? 'video/webm' :
    (ext === 'ogv' || ext === 'ogg') ? 'video/ogg' :
    ''
  );

  if (!allowed.includes(mime)) {
    err.text('Разрешены: PNG, JPG, WebP, SVG, MP4, WebM, OGG');
    return;
  }

  const fd = new FormData();
  fd.append('file', file);

  try {
    const r = await fetch('/api/plan', { method: 'POST', body: fd });
    if (!r.ok) throw new Error('upload failed');
    const data = await r.json();
    const v = data.version ?? Date.now();
    const src = `${data.url}?v=${v}`;
    const img = $('#plan_preview_img');
    const video = $('#plan_preview_video');
    const placeholder = $('#plan_preview_placeholder');

    placeholder.hide();
    const ctype = String(data.type || mime || '').toLowerCase();
    if (ctype.startsWith('video/')) {
      img.hide().removeAttr('src');
      video.attr('src', src).show();
      video.get(0)?.load?.();
    } else {
      video.hide().removeAttr('src');
      img.attr('src', src).show();
    }
  } catch (e) {
    err.text('Загрузка не удалась');
  }
});
}
