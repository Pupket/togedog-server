package pupket.togedogserver.domain.notification.controller.port;

import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDtoForMatching;
import pupket.togedogserver.domain.notification.dto.NotificationResponseDto;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface NotificationService {
    void createToken(Long uuid, String token);

    void deleteToken(Long uuid);

    void sendNotification(NotificationRequestDto notification) throws InterruptedException, ExecutionException;

    void sendNotificationAboutMatching(NotificationRequestDtoForMatching notificationRequestDtoForMatching, User user);

    List<NotificationResponseDto> getUnreceivedNotificationList(CustomUserDetail userDetail, String lastTime);

    Timestamp getParsedLastTime(String lastTime);
}
