import {ReconnectingWebSocket} from "./reconnecting_websocket.js"

export function websocket (path) {
    return new ReconnectingWebSocket(wsPath() + "/socket/" + path);
}

const wsPath = () => {
    const path = document.location.origin;
    if(path.startsWith("http://")){
        return "ws://" + path.split("http://")[1];
    }else {
        return "wss://" + path.split("https://")[1];
    }
}