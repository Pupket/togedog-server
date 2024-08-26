package pupket.togedogserver.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pupket.togedogserver.global.socketio.service.SocketIOHandler;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SocketIOHandler socketIOHandler;

    public void sendNotification(String message) {
        socketIOHandler.onNotificationReceived(message);
    }

}
