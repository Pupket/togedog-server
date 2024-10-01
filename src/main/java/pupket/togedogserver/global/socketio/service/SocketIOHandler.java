package pupket.togedogserver.global.socketio.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pupket.togedogserver.domain.chat.constant.ServerMessage;
import pupket.togedogserver.domain.chat.dto.ChattingDto;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketIOHandler {

    private final SocketIOServer server;
    private final SocketIOService socketIOService;

    @OnConnect
    private ConnectListener onConnected() {
        return client -> {
            String room = client.getHandshakeData().getSingleUrlParam("room");
            String sender = client.getHandshakeData().getSingleUrlParam("sender");
            String receiver = client.getHandshakeData().getSingleUrlParam("receiver");
            client.joinRoom(room);
            socketIOService.saveServerChatting(client, sender + ServerMessage.JOINED, Long.valueOf(room));
            log.info(sender + " joined " + room);
        };
    }

    @OnDisconnect
    private DisconnectListener onDisconnected() {
        return client -> {
            String room = client.getHandshakeData().getSingleUrlParam("room");
            String sender = client.getHandshakeData().getSingleUrlParam("sender");
            String receiver = client.getHandshakeData().getSingleUrlParam("receiver");
            socketIOService.saveServerChatting(client, sender + ServerMessage.LEFT, Long.valueOf(room));
            log.info(sender + " left " + room);
        };
    }

    @OnEvent(value = "chat_received")
    private DataListener<ChattingDto> onChatReceived() {
        return (senderClient, data, ackSender) -> {
            socketIOService.saveChatting(senderClient, data);
            log.info("message from" + data);
        };
    }

}
