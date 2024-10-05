package pupket.togedogserver.domain.chat.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;

@Data
public class KafkaChattingDto {
    private Long userId;
    private String content;
    private String image;
    private Timestamp lastTime;
}
