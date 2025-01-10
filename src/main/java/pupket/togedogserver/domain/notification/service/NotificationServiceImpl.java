package pupket.togedogserver.domain.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pupket.togedogserver.domain.notification.constant.NotificationType;
import pupket.togedogserver.domain.notification.controller.port.NotificationService;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDtoForMatching;
import pupket.togedogserver.domain.notification.dto.NotificationResponseDto;
import pupket.togedogserver.domain.notification.entity.Notification;
import pupket.togedogserver.domain.notification.service.port.NotificationRepository;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.FcmException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

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
    @Transactional
    public void sendNotification(NotificationRequestDto notification) throws InterruptedException, ExecutionException {
        Long roomId = notification.getRoomId();
        log.info("Preparing to send notification for roomId: {}, userId: {}", roomId, notification.getUserId());
        log.info("Notification details: content={}, image={}, lastTime={}", notification.getContent(), notification.getImage(), notification.getLastTime());

        //FcmToken값 DB에서 조회
        String token = getToken(notification.getUserId());

        //Token이 비어있는지 검사
        log.info("Retrieved FCM token for userId: {}", notification.getUserId());
        validateToken(notification.getUserId(), token);

        //전송할 데이터 설정
        Map<String, String> data = new HashMap<>();
        setData(notification, roomId, data);
        log.info("data ={}", data);

        //메세지 생성
        log.info("FCM message payload: {}", data);
        Message firebaseMessage = createFireBaseMessage(notification, token, data);

        //메세지 전송
        log.info("Sending FCM message for userId: {}, roomId: {}, type : {} ", notification.getUserId(), roomId);
        sendMessage(notification, roomId, firebaseMessage);

        //Notification Entity 생성
        Notification notificationEntity = createNotificationEntity(notification, roomId);

        //메세지 전송 성공 후 유저의 알림 리스트 저장
        notificationRepository.save(notificationEntity);
    }

    @Override
    public void sendNotificationAboutMatching(NotificationRequestDtoForMatching notificationRequestDtoForMatching, User user) {

        Long userId = notificationRequestDtoForMatching.getUserId();
        Long boardId = notificationRequestDtoForMatching.getBoardId();
        String token = getToken(userId);

        validateToken(userId, token);

        //전송할 데이터 설정
        Map<String, String> data = new HashMap<>();
        setDataFromMatching(notificationRequestDtoForMatching, data);
        log.info("FCM message payload: {}", data);

        log.info("Sending FCM message for userId: {}, roomId: {}", userId, boardId);
        Message firebaseMessage = createMessageForMatching(notificationRequestDtoForMatching, token, data);

        sendFirebaseMessageFromMatching(firebaseMessage, userId, boardId);

        createAndSaveNotificationEntity(notificationRequestDtoForMatching, user, boardId);

    }

    private void createAndSaveNotificationEntity(NotificationRequestDtoForMatching notificationRequestDtoForMatching, User owner, Long boardId) {
        Notification notification = Notification.builder()
                .title(notificationRequestDtoForMatching.getTitle())
                .type(NotificationType.MATCH)
                .boardId(boardId)
                .image(null)
                .sendTime(Timestamp.from(Instant.now()))
                .user(owner)
                .content(notificationRequestDtoForMatching.getMessage())
                .build();

        notificationRepository.save(notification);
    }

    private static void sendFirebaseMessageFromMatching(Message firebaseMessage, Long userId, Long boardId) {
        try {
            String response = FirebaseMessaging.getInstance().sendAsync(firebaseMessage).get();
            log.info("Successfully sent FCM message. Response: {}", response);
        } catch (InterruptedException e) {
            // InterruptedException 처리
            Thread.currentThread().interrupt(); // 현재 스레드의 interrupt 상태 복구
            log.error("FCM message sending interrupted for userId: {}, boardId: {}. Error: {}",
                    userId, boardId, e.getMessage());
        } catch (ExecutionException e) {
            // ExecutionException 처리
            log.error("Failed to send FCM message for userId: {}, boardId: {}. Error: {}",
                    userId, boardId, e.getMessage());
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("Unexpected error while sending FCM message for userId: {}, boardId: {}. Error: {}",
                    userId, boardId, e.getMessage());
        }
    }

    private static Message createMessageForMatching(NotificationRequestDtoForMatching notificationRequestDtoForMatching, String token, Map<String, String> data) {
        return Message.builder()
                .setToken(token)
                .setAndroidConfig(AndroidConfig.builder()
                        .setTtl(43200000)
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setTitle(notificationRequestDtoForMatching.getTitle())
                                .setBody(notificationRequestDtoForMatching.getMessage())
                                .setImage(null)
                                .build())
                        .putAllData(data)
                        .build())
                .build();
    }

    private static void setDataFromMatching(NotificationRequestDtoForMatching notificationRequestDtoForMatching, Map<String, String> data) {
        data.put("boardId", String.valueOf(notificationRequestDtoForMatching.getBoardId()));
        data.put("message", notificationRequestDtoForMatching.getMessage());
        data.put("sendTime", notificationRequestDtoForMatching.getTimestamp().toString());
        data.put("type" , NotificationType.MATCH.name());
    }

    @Override
    public List<NotificationResponseDto> getUnreceivedNotificationList(CustomUserDetail userDetail, String lastTime) {
        Timestamp parsedLastTime = getParsedLastTime(lastTime);
        User findUser = getFindUser(userDetail.getUuid());

        List<Notification> notificationList = notificationRepository.findAllByUser(findUser);

        return notificationList.stream().filter(
                notification -> notification.getSendTime().after(parsedLastTime)
        ).map(
                notification -> {
                    if(notification.getType().equals(NotificationType.MATCH)) {
                        return NotificationResponseDto.builder()
                                .roomId(null)
                                .boardId(notification.getBoardId())
                                .title(notification.getTitle())
                                .userId(notification.getUser().getUuid())
                                .type(NotificationType.MATCH)
                                .image(null)
                                .lastTime(notification.getSendTime())
                                .content(notification.getContent())
                                .build();
                    }else{
                        return NotificationResponseDto.builder()
                                .userId(notification.getUser().getUuid())
                                .roomId(notification.getRoomId())
                                .boardId(null)
                                .type(NotificationType.CHAT)
                                .title(notification.getTitle())
                                .image(Optional.of(notification.getImage()).orElse(null))
                                .content(Optional.of(notification.getContent()).orElse(null))
                                .lastTime(notification.getSendTime())
                                .build();
                    }
                }
        ).toList();
    }

    @Override
    public Timestamp getParsedLastTime(String lastTime) {
        log.info("Parsing timestamp: {}", lastTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return new Timestamp(dateFormat.parse(lastTime).getTime());
        } catch (Exception e) {
            log.error("Failed to parse timestamp: {}", lastTime, e);
            return new Timestamp(System.currentTimeMillis());
        }
    }

    private Notification createNotificationEntity(NotificationRequestDto notification, Long roomId) {
        String content = getContent(notification);
        String image = getImage(notification);
        String title = getTitle(image, content);
        User findUser = getFindUser(notification.getUserId());
        List<Notification> notificationList = findUser.getNotification();
        Notification notificationEntity = notificationRepository.save(Notification.builder()
                .title(title)
                .type(NotificationType.CHAT)
                .roomId(roomId)
                .boardId(null)
                .content(content)
                .image(image)
                .sendTime(Timestamp.from(Instant.now()))
                .user(findUser)
                .build());
        notificationList.add(notificationEntity);
        userRepository.save(findUser);

        return notificationEntity;
    }

    private User getFindUser(Long userId) {
        return userRepository.findByUuid(userId).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
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

    private String getToken(Long userId) {
        return userRepository.findByUuid(userId)
                .orElseThrow(() -> {
                    log.error("FCM token not found for userId: {}", userId);
                    return new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN);
                }).getFcmToken();
    }

    private static void validateToken(Long userId, String token) {
        if (token == null || token.isEmpty()) {
            log.error("FCM token is null or empty for userId: {}", userId);
            throw new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN);
        }
    }

    private static void setData(NotificationRequestDto notification, Long roomId, Map<String, String> data) {
        data.put("roomId", String.valueOf(roomId));
        data.put("message", notification.getContent());
        data.put("image", notification.getImage() != null ? notification.getImage() : "");
        data.put("timestamp", String.valueOf(notification.getLastTime().getTime()));
        data.put("type", NotificationType.CHAT.name());
    }

    private static Message createFireBaseMessage(NotificationRequestDto notification, String token, Map<String, String> data) {
        String content = getContent(notification);
        String image = getImage(notification);
        String title = "";
        title = getTitle(image, title);
        return Message.builder()
                .setToken(token)
                .setAndroidConfig(AndroidConfig.builder()
                        .setTtl(43200000)
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setTitle(title)
                                .setBody(content)
                                .setImage(image)
                                .build())
                        .putAllData(data)
                        .build())
                .build();
    }

    private static String getTitle(String image, String title) {
        if (!image.isEmpty() && title == null) {
            title = "사진";
        } else {
            title = "새로운 메세지";
        }
        return title;
    }

    private static String getImage(NotificationRequestDto notification) {
        return Optional.of(notification.getImage()).orElse(null);
    }

    private static String getContent(NotificationRequestDto notification) {
        return Optional.of(notification.getContent()).orElse(null);
    }
}