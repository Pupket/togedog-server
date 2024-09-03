package pupket.togedogserver.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.notification.dto.NotificationRequest;
import pupket.togedogserver.domain.user.repository.UserRepository;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {

    private final UserRepository userRepository;

    public void createToken(Long uuid, String token) {
        userRepository.updateFcmTokenByUuid(token, uuid);
    }

    public void deleteToken(Long uuid) {
        userRepository.updateFcmTokenToNullByUuid(uuid);
    }

    public String getToken(Long uuid) {
        return userRepository.findByUuid(uuid).get().getFcmToken();
    }

    public void sendNotification(NotificationRequest notification) throws InterruptedException, ExecutionException {
        String title = notification.getTitle();
        String message = notification.getMessage();
        String image = notification.getImage();
        Message firebaseMessage = Message.builder()
                .setToken(userRepository.findByUuid(notification.getReceiver()).get().getFcmToken())
                .setWebpushConfig(WebpushConfig.builder().putHeader("ttl", "43200")
                        .setNotification(
                                new WebpushNotification(
                                        title,
                                        message,
                                        image
                                ))
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance().sendAsync(firebaseMessage).get();
        log.info("Sent message: " + response);
    }

}
