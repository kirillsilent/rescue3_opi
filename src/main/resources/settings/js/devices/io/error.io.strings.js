const err_audio = 'Аудиокарта недоступна';
const err_camera = 'Видеокамера недоступна';
const err_not_installed = 'Устройство не установлено';
const err_not_available = 'Нет доступных устройств';

export const getErrAudio = () => {
    return err_audio;
};
export const getErrCamera = () => {
    return err_camera;
};

export const getErrNotInstalled = () => {
    return err_not_installed;
}

export const getErrNotAvailable = () => {
    return err_not_available;
}
