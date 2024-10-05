package pupket.togedogserver.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;

@Getter
@Setter
public class ChattingRequestDto {
    Timestamp lastTime;
    Long userId;
    String content;
    MultipartFile image;
}
