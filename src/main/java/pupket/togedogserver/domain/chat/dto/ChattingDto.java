package pupket.togedogserver.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.user.entity.User;

import java.sql.Timestamp;

@Getter
@Setter
public class ChattingDto {
    Timestamp lastTime;
    Long userId;
    String content;
    MultipartFile image;
}
