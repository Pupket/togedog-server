package pupket.togedogserver.global.socketio.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.service.ChatService;

import java.sql.Timestamp;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketIOHandler {

    private final SocketIOServer server;
    private final ChatService chatService;
    private final SocketIOService socketIOService;

    @PostConstruct
    public void init() {
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("chat_received", ChattingRequestDto.class, onChatReceived());
    }
    @OnConnect
    private ConnectListener onConnected() {
        return client -> {
                log.info("Connected to server");
            String room = client.getHandshakeData().getSingleUrlParam("room");
            Timestamp lastTime = Timestamp.valueOf(client.getHandshakeData().getSingleUrlParam("lastTime"));
            client.joinRoom(room);
            List<ChattingResponseDto> chats = chatService.getUndeliveredChats(Long.valueOf(room), lastTime);
            log.info("{}", chats.size());
            for (ChattingResponseDto chat : chats) {
                log.info("채팅내용={}", chat.toString());
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

    private DataListener<ChattingRequestDto> onChatReceived() {
        log.info("onChatReceived");
        return (senderClient, data, ackSender) -> {
            log.info("message from={}" , data);
            socketIOService.sendChatting(senderClient, data);
        };
    }

}
