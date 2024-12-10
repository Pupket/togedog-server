package pupket.togedogserver.domain.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pupket.togedogserver.domain.notification.controller.port.FcmService;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.FcmException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FcmServiceImpl implements FcmService {

    private final UserRepository userRepository;

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
    public void sendNotification(NotificationRequestDto notification) throws InterruptedException, ExecutionException {
        Long roomId = notification.getRoomId();
        log.info("Preparing to send notification for roomId: {}, userId: {}", roomId, notification.getUserId());
        log.info("Notification details: content={}, image={}, lastTime={}", notification.getContent(), notification.getImage(), notification.getLastTime());

        //FcmToken값 DB에서 조회
        String token = getToken(notification);

        //Token이 비어있는지 검사
        log.info("Retrieved FCM token for userId: {}", notification.getUserId());
        validateToken(notification, token);

        //전송할 데이터 설정
        Map<String, String> data = new HashMap<>();
        setData(notification, roomId, data);

        //메세지 생성
        log.info("FCM message payload: {}", data);
        Message firebaseMessage = createFireBaseMessage(notification, token, data);

        //메세지 전송
        log.info("Sending FCM message for userId: {}, roomId: {}", notification.getUserId(), roomId);
        sendMessage(notification, roomId, firebaseMessage);
    }

    private static void sendMessage(NotificationRequestDto notification, Long roomId, Message firebaseMessage) throws InterruptedException, ExecutionException {
        try {
            String response = FirebaseMessaging.getInstance().sendAsync(firebaseMessage).get();
            log.info("Successfully sent FCM message. Response: {}", response);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send FCM message for userId: {}, roomId: {}. Error: {}",
                    notification.getUserId(), roomId, e.getMessage());
            throw e;
        }
    }

    private String getToken(NotificationRequestDto notification) {
       return userRepository.findByUuid(notification.getUserId())
                .orElseThrow(() -> {
                    log.error("FCM token not found for userId: {}", notification.getUserId());
                    return new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN);
                }).getFcmToken();
    }

    private static void validateToken(NotificationRequestDto notification, String token) {
        if (token == null || token.isEmpty()) {
            log.error("FCM token is null or empty for userId: {}", notification.getUserId());
            throw new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN);
        }
    }

    private static void setData(NotificationRequestDto notification, Long roomId, Map<String, String> data) {
        data.put("roomId", String.valueOf(roomId));
        data.put("message", notification.getContent());
        data.put("image", notification.getImage() != null ? notification.getImage() : "");
        data.put("timestamp", String.valueOf(notification.getLastTime().getTime()));
    }

    private static Message createFireBaseMessage(NotificationRequestDto notification, String token, Map<String, String> data) {
        return  Message.builder()
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
    }
}