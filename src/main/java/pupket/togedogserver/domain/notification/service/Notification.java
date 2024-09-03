package pupket.togedogserver.domain.notification.service;

import pupket.togedogserver.domain.notification.dto.NotificationRequest;

import java.util.concurrent.ExecutionException;

public interface Notification {
    public void sendNotification(Long sender, NotificationRequest notification) throws ExecutionException, InterruptedException;
}
