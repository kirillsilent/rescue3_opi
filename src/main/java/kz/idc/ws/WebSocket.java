package kz.idc.ws;

import io.micronaut.http.MediaType;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;


@ServerWebSocket("/socket/{uuid}")
@AllArgsConstructor
public class WebSocket{

    private final WebSocketBroadcaster broadcaster;

    private static final Logger log = LoggerFactory.getLogger(WebSocket.class);

    @OnOpen
    public CompletableFuture<String> onOpen(String uuid) {
        log.info("Connected to topic: " + uuid);
        return broadcaster.broadcastAsync("Connected " + uuid);
    }

    @OnMessage
    public CompletableFuture<Object> onMessage(
            String uuid,
            Object message) {
        return broadcaster.broadcastAsync(message, MediaType.APPLICATION_JSON_TYPE, isValid(uuid));
    }

    @OnClose
    public void onClose(String uuid) {
        log.info("Close client: " + uuid);
    }

    private Predicate<WebSocketSession> isValid(String uuid) {
        return s -> uuid.equalsIgnoreCase(s.getUriVariables().get("uuid", String.class, null));
    }
}
