package pupket.togedogserver.domain.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder(toBuilder = true)
public class NotificationRequestDto {
    Long roomId;
    Long userId;
    String content;
    String image;
    Timestamp lastTime;
}
