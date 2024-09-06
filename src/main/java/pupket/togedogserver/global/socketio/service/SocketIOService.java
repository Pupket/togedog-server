package pupket.togedogserver.global.socketio.service;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.repository.BoardRepository;
import pupket.togedogserver.domain.chat.dto.ChattingDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.entity.Chatting;
import pupket.togedogserver.domain.chat.repository.ChatRoomRepository;
import pupket.togedogserver.domain.chat.service.ChatService;
import pupket.togedogserver.domain.notification.dto.NotificationRequest;
import pupket.togedogserver.domain.notification.service.NotificationServiceImpl;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.repository.OwnerRepository;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.domain.user.repository.mateRepo.MateRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.BoardException;
import pupket.togedogserver.global.exception.customException.ChatException;
import pupket.togedogserver.global.exception.customException.OwnerException;
import pupket.togedogserver.global.s3.util.S3FileUtilImpl;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketIOService {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final MateRepository mateRepository;
    private final BoardRepository boardRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;
    private final NotificationServiceImpl notificationService;
    private final S3FileUtilImpl s3FileUtilImpl;

    public void sendChatting(SocketIOClient senderClient, String chat, String room) {
        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                client.sendEvent("read_message", chat);
            }
        }
    }

    public void saveChatting(SocketIOClient senderClient, ChattingDto data) throws ExecutionException, InterruptedException {
        sendChatting(senderClient, data.getContent(), String.valueOf(data.getChatRoom().getRoomId()));
        String imageUrl = s3FileUtilImpl.upload(data.getImage());
        Chatting chatting = Chatting.userchat()
                .chatRoom(chatRoomRepository.findByRoomId(data.getChatRoom().getRoomId())
                        .orElseThrow(() -> new ChatException(ExceptionCode.NOT_FOUND_CHATROOM)))
                .content(data.getContent())
                .imageURL(imageUrl)
                .sender(data.getSender())
                .receiver(data.getReceiver())
                .writtenTime(new java.util.Date())
                .build();

        chatService.saveChatting(chatting);

        NotificationRequest notification = new NotificationRequest();
        notification.setReceiver(data.getReceiver().getUuid());
        notification.setTitle(data.getSender().getName());
        notification.setMessage(data.getContent());
        notification.setImage(imageUrl);

        notificationService.sendNotification(chatting.getSender().getUuid(), notification);
    }

    public void saveServerChatting(SocketIOClient senderClient, String chat, Long room) {
        var params = senderClient.getHandshakeData().getUrlParams();

        String sender = params.get("sender").stream().collect(Collectors.joining());
        Owner owner = ownerRepository.findByUser(
                userRepository.findByUuid(Long.valueOf(sender))
                        .orElseThrow(() -> new OwnerException(ExceptionCode.NOT_FOUND_MEMBER)
                        )).orElseThrow(() -> new OwnerException(ExceptionCode.NOT_FOUND_MEMBER));

        String receiver = params.get("receiver").stream().collect(Collectors.joining());
        Mate mate = mateRepository.findByUser(
                userRepository.findByUuid(
                        Long.valueOf(receiver)).orElseThrow(() -> new OwnerException(ExceptionCode.NOT_FOUND_MEMBER))
        ).orElseThrow(() -> new OwnerException(ExceptionCode.NOT_FOUND_MEMBER));

        String boardId = params.get("boardId").stream().collect(Collectors.joining());
        Board board = boardRepository.findByBoardId(Long.valueOf(boardId))
                .orElseThrow(() -> new BoardException(ExceptionCode.INVALID_BOARD));

        chatRoomRepository.findByRoomId(room)
                .orElse(chatRoomRepository.save(new ChatRoom().createChatRoom(owner, mate, board)));

        sendChatting(senderClient, chat, String.valueOf(room));

        Chatting chatting = Chatting.serverchat()
                .chatRoom(chatRoomRepository.findByRoomId(room)
                        .orElseThrow(() -> new ChatException(ExceptionCode.NOT_FOUND_CHATROOM)))
                .writtenTime(new Date())
                .build();
        chatService.saveChatting(chatting);
    }

}
