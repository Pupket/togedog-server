package pupket.togedogserver.domain.notification.dto;

import lombok.Getter;

@Getter
public class NotificationRequest {
    Long receiver;
    String title;
    String message;
    String image;
}
