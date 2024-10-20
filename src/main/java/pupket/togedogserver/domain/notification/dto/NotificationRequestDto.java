package pupket.togedogserver.domain.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class NotificationRequestDto {
    Long receiver;
    String title;
    String message;
    String image;
}
