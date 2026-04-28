import "../../../jquery/jquery-3.2.1.min.js";
import {httpGetAsync, httpPutAsync} from "../../../rs/requests.js";
import {getIncidentSettings, setIncidentSettings} from "../../../rs/incident/incident.paths.js";
import {getApiSettings, setEventServerUrl} from "../../../rs/api/api.paths.js";

const event_server_url_coords = $('#event_server_url_coords');
const device_sn_coords = $('#device_sn_coords');

const inp_date_time = $('#incident_date_time');
const inp_gts_phone = $('#incident_gts_phone');
const inp_gts_fio = $('#incident_gts_fio');
const inp_gts_block = $('#incident_gts_block');
const inp_gts_home = $('#incident_gts_home');
const inp_gts_flat = $('#incident_gts_flat');
const inp_descr_event = $('#incident_descr_event');
const inp_street = $('#incident_street');
const inp_cross_street = $('#incident_cross_street');
const inp_numeric_home = $('#incident_numeric_home');
const inp_block = $('#incident_block');
const inp_numeric_flat = $('#incident_numeric_flat');
const inp_descr_place = $('#incident_descr_place');
const inp_status_card = $('#incident_status_card');
const inp_xcoord = $('#incident_xcoord');
const inp_ycoord = $('#incident_ycoord');
const inp_user_id = $('#incident_user_id');
const inp_category_code = $('#incident_category_code');
const inp_region_id = $('#incident_region_id');
const inp_sid = $('#incident_sid');
const inp_gts_street = $('#incident_gts_street');

const str = (v) => (v === null || v === undefined) ? '' : String(v);
const intOrZero = (v) => {
  const n = parseInt(String(v ?? '').trim(), 10);
  return Number.isFinite(n) ? n : 0;
};
const floatOrZero = (v) => {
  const n = parseFloat(String(v ?? '').trim());
  return Number.isFinite(n) ? n : 0;
};

// ====== date_time: автозаполнение/обновление времени ======
let _dateTimeTimer = null;

const _pad2 = (n) => String(n).padStart(2, '0');
const _formatNowDateTime = () => {
  const d = new Date();
  // Формат: YYYY-MM-DD HH:mm:ss (локальное время)
  return (
    `${d.getFullYear()}-${_pad2(d.getMonth() + 1)}-${_pad2(d.getDate())} ` +
    `${_pad2(d.getHours())}:${_pad2(d.getMinutes())}:${_pad2(d.getSeconds())}`
  );
};

const _tickDateTime = () => {
  if (!inp_date_time || inp_date_time.length === 0) return;
  inp_date_time.val(_formatNowDateTime());
};

export const startIncidentDateTimeAutoUpdate = () => {
  // Стартуем только один таймер
  if (_dateTimeTimer) return;
  _tickDateTime(); // сразу выставим
  _dateTimeTimer = setInterval(_tickDateTime, 1000);
};

export const stopIncidentDateTimeAutoUpdate = () => {
  if (!_dateTimeTimer) return;
  clearInterval(_dateTimeTimer);
  _dateTimeTimer = null;
};

export const loadIncidentSettingsUI = () => {
  // date_time должен показывать актуальное время на вкладке «Координаты»
  startIncidentDateTimeAutoUpdate();
  httpGetAsync(getIncidentSettings(), setIncidentSettingsUI, null);
  httpGetAsync(getApiSettings(), setEventServerUrlUI, null);
};

export const setEventServerUrlUI = (data) => {
  // settings_api/get -> ApiDTO { eventServerUrl: "http://..." }
  const url = (data && (data.eventServerUrl ?? data.event_server_url)) ? (data.eventServerUrl ?? data.event_server_url) : '';
  event_server_url_coords.val(str(url));

  const sn = (data && (data.deviceSn ?? data.device_sn)) ? (data.deviceSn ?? data.device_sn) : '';
  device_sn_coords.val(str(sn));
};

export const setIncidentSettingsUI = (data) => {
  // данные приходят в формате IncidentDTO (Sid/Gts_street через @JsonProperty)
  inp_date_time.val(str(data?.date_time));
  inp_gts_phone.val(str(data?.gts_phone));
  inp_gts_fio.val(str(data?.gts_fio));
  inp_gts_block.val(str(data?.gts_block));
  inp_gts_home.val(str(data?.gts_home));
  inp_gts_flat.val(str(data?.gts_flat));
  inp_descr_event.val(str(data?.descr_event));
  inp_street.val(str(data?.street));
  inp_cross_street.val(str(data?.cross_street));
  inp_numeric_home.val(str(data?.numeric_home));
  inp_block.val(str(data?.block));
  inp_numeric_flat.val(str(data?.numeric_flat));
  inp_descr_place.val(str(data?.descr_place));
  inp_status_card.val(str(data?.status_card));
  inp_xcoord.val(str(data?.xcoord));
  inp_ycoord.val(str(data?.ycoord));
  inp_user_id.val(str(data?.user_id));
  inp_category_code.val(str(data?.category_code));
  inp_region_id.val(str(data?.region_id));
  inp_sid.val(str(data?.Sid));
  inp_gts_street.val(str(data?.Gts_street));
};

export const get_values_incident = () => {
  const incident = {};

  incident.date_time = str(inp_date_time.val());
  incident.gts_phone = str(inp_gts_phone.val());
  incident.gts_fio = str(inp_gts_fio.val());
  incident.gts_block = str(inp_gts_block.val());
  incident.gts_home = str(inp_gts_home.val());
  incident.gts_flat = str(inp_gts_flat.val());

  incident.descr_event = str(inp_descr_event.val());
  incident.street = str(inp_street.val());
  incident.cross_street = str(inp_cross_street.val());
  incident.numeric_home = str(inp_numeric_home.val());
  incident.block = str(inp_block.val());
  incident.numeric_flat = str(inp_numeric_flat.val());
  incident.descr_place = str(inp_descr_place.val());
  incident.status_card = intOrZero(inp_status_card.val());

  incident.xcoord = floatOrZero(inp_xcoord.val());
  incident.ycoord = floatOrZero(inp_ycoord.val());

  incident.user_id = str(inp_user_id.val());
  incident.category_code = intOrZero(inp_category_code.val());
  incident.region_id = intOrZero(inp_region_id.val());

  incident.Sid = intOrZero(inp_sid.val());
  incident.Gts_street = str(inp_gts_street.val());

  return incident;
};

export const putIncidentSettingsUI = (onSaved) => {
  const incident = get_values_incident();
  httpPutAsync(setIncidentSettings(), () => {
    if (typeof onSaved === 'function') onSaved();
  }, incident);
};

export const putEventServerUrlFromCoords = (onSaved) => {
  const payload = {
    url: str(event_server_url_coords.val()),
    deviceSn: str(device_sn_coords.val())
  };
  httpPutAsync(setEventServerUrl(), () => {
    if (typeof onSaved === 'function') onSaved();
  }, payload);
};

