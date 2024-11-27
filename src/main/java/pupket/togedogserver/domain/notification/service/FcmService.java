package pupket.togedogserver.domain.notification.service;

import com.google.firebase.messaging.*;
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
        
        String token = userRepository.findByUuid(notification.getUserId())
            .orElseThrow(() -> new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN))
            .getFcmToken();

        if (token == null || token.isEmpty()) {
            log.error("FCM token is null or empty for userId: {}", notification.getUserId());
            throw new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN);
        }

        Map<String, String> data = new HashMap<>();
        data.put("roomId", String.valueOf(roomId));
        data.put("message", message);
        data.put("image", image != null ? image : "");
        data.put("timestamp", String.valueOf(lastTime.getTime()));

        Message firebaseMessage = Message.builder()
            .setToken(token)
            .setAndroidConfig(AndroidConfig.builder()
                .setTtl(43200000)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setTitle("새로운 메시지")
                    .setBody(message)
                    .setImage(image)
                    .build())
                .putAllData(data)
                .build())
            .build();

        String response = FirebaseMessaging.getInstance().sendAsync(firebaseMessage).get();
        log.info("Sent message: {}", response);
    }

}
