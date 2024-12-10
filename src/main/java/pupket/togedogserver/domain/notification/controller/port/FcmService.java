package pupket.togedogserver.domain.notification.controller.port;

import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;

import java.util.concurrent.ExecutionException;

public interface FcmService {
    void createToken(Long uuid, String token);

    void deleteToken(Long uuid);

    void sendNotification(NotificationRequestDto notification) throws InterruptedException, ExecutionException;
}
