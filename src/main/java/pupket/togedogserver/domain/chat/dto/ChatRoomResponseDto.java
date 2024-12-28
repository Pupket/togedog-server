package pupket.togedogserver.domain.chat.dto;

import lombok.Builder;
import lombok.Data;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.user.entity.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

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

    public static ChatRoomResponseDto to(ChatRoom room, User sender, User receiver, List<ChattingResponseDto> unreceivedMessages, ChattingResponseDto lastMessage) {

        return ChatRoomResponseDto.builder()
                .roomId(room.getRoomId())
                .title(room.getTitle())
                .lastTime(Optional.ofNullable(lastMessage).map(ChattingResponseDto::getLastTime).orElse(null))
                .sender(sender.getNickname())
                .senderImage(sender.getProfileImage().isEmpty() ? null : sender.getProfileImage())
                .receiver(receiver.getNickname())
                .receiverImage(receiver.getProfileImage().isEmpty() ? null : receiver.getProfileImage())
                .unreceivedMessageCount(unreceivedMessages.size())
                .lastMessage(Optional.ofNullable(lastMessage).map(ChattingResponseDto::getContent).orElse(null))
                .build();

    }
}
