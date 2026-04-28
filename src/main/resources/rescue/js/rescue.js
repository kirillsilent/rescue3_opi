import "../../jquery/jquery-3.2.1.min.js";
import "../../jquery/jquery.marquee.min.js"
import {httpGetAsync, move} from "../../rs/requests.js";
import {setLocalStorage} from "../../utils/storage/storage.js";
import {getSettings} from "../../views.paths/paths.js";
import {getMarquee} from "../../rs/marquee/marquee.paths.js";
import {getStatusNetwork, getStatusSip} from "../../rs/status/status.paths.js";
import {key_tab, previous_tab} from "../../utils/storage/keys.js";
import {websocket} from "../../utils/socket/init.js";
import {clearTimer, startTimer} from "../../utils/timer/timer.interval.js";
import {rescue} from "../../utils/socket/socket.paths.js";


const settings = $('#settings');
const msg = $('#msg');
const incoming = $('#container_incoming');
const incident = $('#incident');
const left = $('#left');
const sip = $('#sip_register');
const network = $('#network');
const localhost = $('#localhost');
const plan = $('#plan');
let ws = null;
let timer = null;
let planRefreshTimer = null;
let planLastKey = null;

$(document).ready(() => {

    ws = websocket(rescue);

    ws.onopen = () => {
        console.log('Connection: OK.');
        changeCSSStatus(localhost, 'localhost_online', 'localhost_offline');
    };

    ws.onmessage = (message) => {
        let obj = message.data;
        try {
            obj = JSON.parse(obj);
            if (obj.hasOwnProperty('marquee')) {
                if (obj.marquee !== ' ') {
                    setMarquee(obj.marquee);
                } else {
                    left.empty();
                    left.hide();
                }
            } else if (obj.hasOwnProperty('type')) {
                if (obj.type === 'plan') {
                    plan.prop("src", '../rescue/plan?rand=' + Math.random());
                }
            } else if (obj.hasOwnProperty('incoming')) {
                incomingCallUI();
            } else if (obj.hasOwnProperty('clearUI')) {
                clearIncomingCallUI();
            } else if (obj.hasOwnProperty('emergencyCategoryName')) {
                if (obj.active) {
                    clearIncomingCallUI();
                    incidentUI(obj);
                } else {
                    clearIncidentUI();
                }
            }
        } catch (exception) {
        }
    }

    ws.onerror = () => {
        console.clear();
        changeCSSStatus(localhost, 'localhost_offline', 'localhost_online');
    }

    httpGetAsync(getMarquee());
    plan.prop("src", '../rescue/plan?rand=' + Math.random());
    setTimeout(() => {
        checkAllStates();
    }, 2000);

});

const checkAllStates = () => {
    httpGetAsync(getStatusSip(), setSipStatus);
    httpGetAsync(getStatusNetwork(), setNetworkStatus);
    if (timer === null) {
        timer = startTimer(timer, checkAllStates, null);
    }
}

export const setSipStatus = (data) => {
    if (data.status) {
        changeCSSStatus(sip, 'sip_reg_online', 'sip_reg_offline');
    } else {
        changeCSSStatus(sip, 'sip_reg_offline', 'sip_reg_online');
    }
}

export const setNetworkStatus = (data) => {
    if (data.status) {
        changeCSSStatus(network, 'net_online', 'net_offline');
    } else {
        changeCSSStatus(network, 'net_offline', 'net_online');
    }
}

const incidentUI = (data) => {
    left.hide();
    msg.html(data.emergencyCategoryName);
    changeCSSStatus(incident, 'alert', 'incoming');
    incident.css('display', 'block');
}

const incomingCallUI = () => {
    incoming.css("visibility", "visible");
}

const clearIncomingCallUI = () => {
    incoming.css("visibility", "hidden");
}

const clearIncidentUI = () => {
    incident.css('display', 'none');
    left.show();
}

const changeCSSStatus = (obj, add, remove) => {
    obj.removeClass(remove);
    obj.addClass(add);
}

const setMarquee = (s) => {
    left.show();
    left.empty();
    left.html(s);
}

const closeSocket = () => {
    if (ws !== null) {
        ws.close();
    }
}

settings.click(() => {
    // Settings page will request password itself.
    closeSocket();
    clearTimer(timer);
    setLocalStorage(previous_tab, 'tab_network');
    setLocalStorage(key_tab, 'tab_network');
    move(getSettings());
});
document.addEventListener('DOMContentLoaded', () => {
    initPlanMedia();
    if (planRefreshTimer === null) {
      planRefreshTimer = setInterval(initPlanMedia, 5000);
    }
  });
  
  async function initPlanMedia() {
    const imgEl = document.getElementById('plan');
    const vidEl = document.getElementById('plan_video');
  
    try {
      const r = await fetch('/api/plan', { cache: 'no-store' });
      if (!r.ok) throw new Error('no plan');
      // ожидаем { url, version, type }  (type опционален)
      const data = await r.json();
  
      if (!data || !data.url) {
        // ничего не пришло — прячем оба
        imgEl.style.display = 'none';
        vidEl.style.display = 'none';
        return;
      }
  
      const v = data.version ?? Date.now();
      const url = appendVersion(data.url, v);
      const mime = data.type || guessMimeByExt(data.url);
      const planKey = `${data.url}|${v}|${mime}`;
      if (planLastKey === planKey) {
        return;
      }
      planLastKey = planKey;
  
      if (isVideoMime(mime)) {
        // показать видео
        imgEl.style.display = 'none';
        vidEl.style.display = 'block';
  
        // простой MP4/WebM
        if (mime === 'video/mp4' || mime === 'video/webm' || mime === 'video/ogg') {
          vidEl.src = url;
          // автоплей может требовать muted=true (у нас уже стоит)
          try { await vidEl.play(); } catch (_) {}
        } else if (mime === 'application/vnd.apple.mpegurl' || data.url.endsWith('.m3u8')) {
          // HLS: если подключишь hls.js, то вот так
          if (window.Hls && window.Hls.isSupported()) {
            const hls = new window.Hls({ autoStartLoad: true });
            hls.loadSource(url);
            hls.attachMedia(vidEl);
            hls.on(window.Hls.Events.MANIFEST_PARSED, () => { try { vidEl.play(); } catch(_){} });
          } else if (vidEl.canPlayType('application/vnd.apple.mpegurl')) {
            // Safari
            vidEl.src = url;
            try { await vidEl.play(); } catch (_) {}
          } else {
            // fallback: не умеем HLS — спрячем видео
            vidEl.style.display = 'none';
            imgEl.style.display = 'none';
            console.warn('HLS не поддерживается и hls.js не подключён');
          }
        } else {
          // неизвестное видео
          vidEl.style.display = 'none';
          imgEl.style.display = 'none';
        }
  
        // на всякий случай перезапускаем по окончанию (доп. страховка к loop)
        vidEl.addEventListener('ended', () => {
          vidEl.currentTime = 0;
          vidEl.play().catch(()=>{});
        });
  
        // если источник недоступен
        vidEl.addEventListener('error', () => {
          vidEl.style.display = 'none';
          imgEl.style.display = 'none';
        });
  
      } else {
        // показать картинку
        vidEl.pause();
        vidEl.removeAttribute('src');
        vidEl.load();
        vidEl.style.display = 'none';
  
        imgEl.src = url;
        imgEl.style.display = 'block';
        imgEl.onerror = () => { imgEl.style.display = 'none'; };
      }
    } catch (e) {
      // на ошибке прячем оба
      document.getElementById('plan').style.display = 'none';
      document.getElementById('plan_video').style.display = 'none';
      planLastKey = null;
    }
  }
  
  function appendVersion(u, v) {
    try {
      const url = new URL(u, window.location.origin);
      url.searchParams.set('v', String(v));
      return url.toString();
    } catch {
      // относительный путь без протокола
      const sep = u.includes('?') ? '&' : '?';
      return `${u}${sep}v=${encodeURIComponent(v)}`;
    }
  }
  
  function guessMimeByExt(u) {
    const l = u.toLowerCase();
    if (l.endsWith('.mp4')) return 'video/mp4';
    if (l.endsWith('.webm')) return 'video/webm';
    if (l.endsWith('.ogg') || l.endsWith('.ogv')) return 'video/ogg';
    if (l.endsWith('.m3u8')) return 'application/vnd.apple.mpegurl';
    if (l.endsWith('.jpg') || l.endsWith('.jpeg')) return 'image/jpeg';
    if (l.endsWith('.png')) return 'image/png';
    if (l.endsWith('.webp')) return 'image/webp';
    if (l.endsWith('.svg')) return 'image/svg+xml';
    return '';
  }
  
  function isVideoMime(m) {
    return m.startsWith('video/') || m === 'application/vnd.apple.mpegurl';
  }
  
