package pupket.togedogserver.domain.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pupket.togedogserver.domain.notification.controller.port.FcmService;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.FcmException;
import pupket.togedogserver.global.websocket.WebSocketEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FcmServiceImpl implements FcmService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplateForUserStatus;
    private final WebSocketEventListener webSocketEventListener;


    @Override
    public void createToken(Long uuid, String token) {
        log.info("Updating FCM token for userId: {}, token: {}", uuid, token);
        userRepository.updateFcmTokenByUuid(token, uuid);
        log.info("Successfully updated FCM token for userId: {}", uuid);
    }

    @Override
    public void deleteToken(Long uuid) {
        log.info("Deleting FCM token for userId: {}", uuid);
        userRepository.updateFcmTokenToNullByUuid(uuid);
        log.info("Successfully deleted FCM token for userId: {}", uuid);
    }

    @Override
    public void sendNotification(NotificationRequestDto notification, Long roomId) throws InterruptedException, ExecutionException {
        log.info("Preparing to send notification for roomId: {}, userId: {}", roomId, notification.getUserId());
        log.debug("Notification details: content={}, image={}, lastTime={}", notification.getContent(), notification.getImage(), notification.getLastTime());

        String sessionId = redisTemplateForUserStatus.opsForValue().get("user:session:" + notification.getUserId());
        if (sessionId == null || !webSocketEventListener.isSessionConnected(sessionId)) {
            log.warn("User {} is offline. Sending notification.", notification.getUserId());
            String key = "offline:notifications:" + notification.getUserId();
            if (notification.getContent().isEmpty() && !notification.getImage().isEmpty()) {
                redisTemplateForUserStatus.opsForValue().set(key, "사진");
            }else{
                redisTemplateForUserStatus.opsForValue().set(key, notification.getContent());
            }
            redisTemplateForUserStatus.expire(key,14, TimeUnit.DAYS); //14일 유지
            return;
        }

        String token = userRepository.findByUuid(notification.getUserId())
                .orElseThrow(() -> {
                    log.error("FCM token not found for userId: {}", notification.getUserId());
                    return new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN);
                }).getFcmToken();

        if (token == null || token.isEmpty()) {
            log.error("FCM token is null or empty for userId: {}", notification.getUserId());
            throw new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN);
        }

        log.info("Retrieved FCM token for userId: {}", notification.getUserId());

        Map<String, String> data = new HashMap<>();
        data.put("roomId", String.valueOf(roomId));
        data.put("message", notification.getContent());
        data.put("image", notification.getImage() != null ? notification.getImage() : "");
        data.put("timestamp", String.valueOf(notification.getLastTime().getTime()));

        log.debug("FCM message payload: {}", data);

        Message firebaseMessage = Message.builder()
                .setToken(token)
                .setAndroidConfig(AndroidConfig.builder()
                        .setTtl(43200000)
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setTitle("새로운 메시지")
                                .setBody(notification.getContent())
                                .setImage(notification.getImage())
                                .build())
                        .putAllData(data)
                        .build())
                .build();

        log.info("Sending FCM message for userId: {}, roomId: {}", notification.getUserId(), roomId);

        try {
            String response = FirebaseMessaging.getInstance().sendAsync(firebaseMessage).get();
            log.info("Successfully sent FCM message. Response: {}", response);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send FCM message for userId: {}, roomId: {}. Error: {}",
                    notification.getUserId(), roomId, e.getMessage());
            throw e;
        }
    }
}