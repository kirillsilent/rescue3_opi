export const setLocalStorage = (key, value) => {
    localStorage.setItem(key, value);
};

export const getValue = key => localStorage.getItem(key);