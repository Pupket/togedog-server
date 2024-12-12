package pupket.togedogserver.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.notification.constant.NotificationType;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {

    Long roomId;
    Long boardId;
    NotificationType type;
    Long userId;
    String title;
    String content;
    String image;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSX", timezone = "UTC") // ISO 8601 형식
    Timestamp lastTime;

    public static NotificationResponseDto to(ChattingRequestDto message, ChatRoom findChatRoom, Timestamp parsedLastTime, Long receiver) {
        return NotificationResponseDto.builder()
                .roomId(findChatRoom.getRoomId())
                .userId(receiver)
                .content(message.getContent())
                .image(message.getImage())
                .lastTime(parsedLastTime)
                .build();
    }
}
