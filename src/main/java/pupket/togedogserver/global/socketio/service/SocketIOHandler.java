package pupket.togedogserver.global.socketio.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pupket.togedogserver.domain.chat.constant.ServerMessage;
import pupket.togedogserver.domain.chat.dto.ChattingDto;

@Component
@Slf4j
public class SocketIOHandler {

    private final SocketIOServer server;
    private final SocketIOService socketIOService;

    public SocketIOHandler(SocketIOServer server, SocketIOService socketIOService) {
        this.server = server;
        this.socketIOService = socketIOService;
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("chat_received", ChattingDto.class, onChatReceived());
    }

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

    private DisconnectListener onDisconnected() {
        return client -> {
            String room = client.getHandshakeData().getSingleUrlParam("room");
            String sender = client.getHandshakeData().getSingleUrlParam("sender");
            String receiver = client.getHandshakeData().getSingleUrlParam("receiver");
            socketIOService.saveServerChatting(client, sender + ServerMessage.LEFT, Long.valueOf(room));
            log.info(sender + " left " + room);
        };
    }

    private DataListener<ChattingDto> onChatReceived() {
        return (senderClient, data, ackSender) -> {
            socketIOService.saveChatting(senderClient, data);
            log.info("message from" + data);
        };
    }

}
