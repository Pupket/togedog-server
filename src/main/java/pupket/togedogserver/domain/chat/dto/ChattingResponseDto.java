package pupket.togedogserver.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class ChattingResponseDto {
    Timestamp lastTime;
    Long userId;
    String content;
    String image;
}
