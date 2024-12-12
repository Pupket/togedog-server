package pupket.togedogserver.domain.notification.controller.port;

import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.notification.dto.NotificationResponseDto;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface NotificationService {
    void createToken(Long uuid, String token);

    void deleteToken(Long uuid);

    void sendNotification(NotificationRequestDto notification) throws InterruptedException, ExecutionException;

    List<NotificationResponseDto> getUnreceivedNotificationList(CustomUserDetail userDetail, String lastTime);

    Timestamp getParsedLastTime(String lastTime);
}
