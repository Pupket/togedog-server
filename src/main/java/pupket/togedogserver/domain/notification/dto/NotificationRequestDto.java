package pupket.togedogserver.domain.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequestDto {
    Long receiver;
    String title;
    String message;
    String image;
}
