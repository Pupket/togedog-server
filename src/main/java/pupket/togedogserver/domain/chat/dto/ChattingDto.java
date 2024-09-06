package pupket.togedogserver.domain.chat.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.user.entity.User;

@Getter
public class ChattingDto {
    ChatRoom chatRoom;
    String content;
    MultipartFile image;
    User sender;
    User receiver;
}
