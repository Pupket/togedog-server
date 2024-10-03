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
import pupket.togedogserver.domain.chat.dto.ChattingDto;

import java.sql.Timestamp;
import java.util.List;

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
            client.joinRoom(room);
            List<ChattingDto> chats = socketIOService.fetchBacklogChats(room);
            for (ChattingDto chat : chats) {
                client.sendEvent("chatMessage", chat);
            }
        };
    }

    @OnDisconnect
    private DisconnectListener onDisconnected() {
        return client -> {
            String room = client.getHandshakeData().getSingleUrlParam("room");
            client.leaveRoom(room);
        };
    }

    @OnEvent(value = "chat_received")
    private DataListener<ChattingDto> onChatReceived() {
        return (senderClient, data, ackSender) -> {
            socketIOService.sendChatting(senderClient, data);
            log.info("message from" + data);
        };
    }

}
