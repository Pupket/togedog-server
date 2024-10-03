package pupket.togedogserver.domain.chat.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;

@Data
public class KafkaChattingDto {
    private Long sender;
    private Long receiver;
    private String content;
    private MultipartFile image;
    private Timestamp lastTime;
}
