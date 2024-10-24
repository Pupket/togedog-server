package pupket.togedogserver.domain.notification.service;

import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;

import java.util.concurrent.ExecutionException;

public interface NotificationService {
     void sendNotification(Long sender, NotificationRequestDto notification) throws ExecutionException, InterruptedException;
}
