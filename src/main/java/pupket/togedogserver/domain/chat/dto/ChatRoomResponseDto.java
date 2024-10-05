package pupket.togedogserver.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class ChatRoomResponseDto {
    Long roomId;
    String content;
    Timestamp lastTime;
    String nickname;
}
