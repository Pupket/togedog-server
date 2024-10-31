package pupket.togedogserver.domain.chat.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ChatRoomResponseDto {
    Long roomId;
    String title;
    String sender;
    Timestamp lastTime;
}
