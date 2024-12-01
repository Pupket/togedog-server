package pupket.togedogserver.domain.notification.dto;

import lombok.Builder;
import lombok.Data;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;

import java.sql.Timestamp;

@Data
@Builder(toBuilder = true)
public class NotificationRequestDto {
    Long roomId;
    Long userId;
    String content;
    String image;
    Timestamp lastTime;

    public static NotificationRequestDto to(ChattingRequestDto message, ChatRoom findChatRoom, Timestamp parsedLastTime, Long receiver) {
        return NotificationRequestDto.builder()
                .content(message.getContent())
                .userId(receiver)
                .image(message.getImage())
                .roomId(findChatRoom.getRoomId())
                .lastTime(parsedLastTime)
                .build();
    }
}
