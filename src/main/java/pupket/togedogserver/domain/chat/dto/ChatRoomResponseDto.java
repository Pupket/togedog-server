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
    Timestamp lastTime;
    int unreceivedMessageCount;
     String lastMessage;

    public static ChatRoomResponseDto to(ChatRoom room, User owner, User mate, List<ChattingResponseDto> unreceivedMessages) {
        return ChatRoomResponseDto.builder()
                .roomId(room.getRoomId())
                .title(room.getTitle())
                .lastTime(unreceivedMessages.isEmpty() ? null : unreceivedMessages.get(0).getLastTime())
                .sender(owner.getNickname())
                .senderImage(owner.getProfileImage().isEmpty() ? null : owner.getProfileImage())
                .receiver(mate.getNickname())
                .receiverImage(mate.getProfileImage().isEmpty() ? null : mate.getProfileImage())
                .unreceivedMessageCount(unreceivedMessages.size())
                .lastMessage(unreceivedMessages.isEmpty() ? null : unreceivedMessages.get(0).getContent())
                .build();

    }
}
