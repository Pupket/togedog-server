package pupket.togedogserver.domain.chat.dto;

import lombok.Builder;
import lombok.Data;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.user.entity.User;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
public class ChatRoomResponseDto {
    Long roomId;
    String title;
    String senderImage;
    String sender;
    String receiver;
    String receiverImage;
     int unreceivedMessageCount;
     String lastMessage;

    public static ChatRoomResponseDto to(ChatRoom room, User findSender, User findReceiver, List<ChattingResponseDto> unreceivedMessages) {
        return ChatRoomResponseDto.builder()
                .roomId(room.getRoomId())
                .title(room.getTitle())
                .sender(findSender.getNickname())
                .senderImage(findSender.getProfileImage().isEmpty() ? null : findSender.getProfileImage())
                .receiver(findReceiver.getNickname())
                .receiverImage(findSender.getProfileImage().isEmpty() ? null : findReceiver.getProfileImage())
                .unreceivedMessageCount(unreceivedMessages.size())
                .lastMessage(unreceivedMessages.isEmpty() ? null : unreceivedMessages.get(0).getContent())
                    .build();

    }
}
