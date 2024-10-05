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
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;

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
            List<ChattingResponseDto> chats = socketIOService.fetchBacklogChats(room);
            for (ChattingResponseDto chat : chats) {
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
    private DataListener<ChattingRequestDto> onChatReceived() {
        return (senderClient, data, ackSender) -> {
            socketIOService.sendChatting(senderClient, data);
            log.info("message from" + data);
        };
    }

}
