package pupket.togedogserver.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.FcmException;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
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

    public void sendNotification(NotificationRequestDto notification, Long roomId) throws InterruptedException, ExecutionException {
        String message = notification.getContent();
        String image = notification.getImage();
        Timestamp lastTime = notification.getLastTime();
        Map<String, String> data = new HashMap<>();
        data.put("roomId", String.valueOf(roomId));
        Message firebaseMessage = Message.builder()
                .setToken(userRepository.findByUuid(notification.getUserId()).orElseThrow(
                        ()-> new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN)
                ).getFcmToken())
                .setWebpushConfig(WebpushConfig.builder().putHeader("ttl", "43200")
                        .setNotification(
                                WebpushNotification.builder()
                                        .setTitle(message)
                                        .setData(message)
                                        .setImage(image)
                                        .setTimestampMillis(lastTime.getTime())
                                        .build())
                        .putAllData(data)
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance().sendAsync(firebaseMessage).get();
        log.info("Sent message: {}", response);
    }

}
