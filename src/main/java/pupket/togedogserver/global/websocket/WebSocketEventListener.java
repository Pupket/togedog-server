package pupket.togedogserver.global.websocket;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.FcmException;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.jwt.util.JwtUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final Set<String> connectedSessions = new HashSet<>();
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtTokenProvider;  // JWT 토큰 파싱을 위한 JwtTokenProvider
    private final JwtService jwtService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    // WebSocket 연결 시 세션 ID 저장 및 사용자 상태를 "online"으로 설정
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId(); // 세션 아이디 헤더
        String token = headerAccessor.getFirstNativeHeader("Authorization"); //어세스토큰 헤더

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        log.info("WebSocket 연결 이벤트 수신: Session ID = {}, Authorization 헤더 = {}", sessionId, token);
        Long userId = 0L;

        // SecurityContext에서 인증 정보 가져오기
        try {
            userId = jwtService.getUserIdFromToken(token);
            log.info("WebSocket 연결: User ID = {}, Session ID = {}", userId, sessionId);
        } catch (Exception e) {
            log.warn("잘못된 형식  = {} ", e.getMessage());
        }

        // Redis에 사용자와 세션 매핑
        try {
            redisTemplate.opsForValue().set("user:session:" + userId, sessionId, 12, TimeUnit.DAYS);
            redisTemplate.opsForValue().set("session:user:" + sessionId, userId.toString(), 12, TimeUnit.DAYS);
            redisTemplate.opsForValue().set("session:status:" + sessionId, "online", 1, TimeUnit.DAYS);
            log.info("Redis에 사용자 매핑 완료: user:session:{} -> {}, session:user:{} -> {}", userId, sessionId, sessionId, userId);

            // 미수신 알림 처리
//            processOfflineNotifications(userId, sessionId);

        } catch (Exception e) {
            log.error("WebSocket 연결 처리 중 예외 발생: Session ID = {}, Error = {}", sessionId, e.getMessage(), e);
        }

        log.info("WebSocket 연결됨: 세션 ID = {}", sessionId);
        //session 연결
        connectedSessions.add(sessionId);
    }

    // Redis에 저장된 미수신 알림을 클라이언트로 전송하는 메서드
    private void processOfflineNotifications(Long userId, String sessionId) throws ExecutionException, InterruptedException {
        String key = "offline:notifications:" + userId;

        // Redis에서 미수신 알림 조회
        List<String> offlineNotifications = redisTemplate.opsForList().range(key, 0, -1);
        if (offlineNotifications != null && !offlineNotifications.isEmpty()) {
            log.info("미수신 알림 발견: User ID = {}, 알림 개수 = {}", userId, offlineNotifications.size());

            for (String notificationJson : offlineNotifications) {
                // 클라이언트로 알림 전송
                sendNotificationToClient(userId, notificationJson);
            }

            // 알림 전송 후 Redis에서 삭제
            redisTemplate.delete(key);
            log.info("Redis에서 미수신 알림 삭제 완료: User ID = {}", userId);
        } else {
            log.info("미수신 알림 없음: User ID = {}", userId);
        }
    }

    private void sendNotificationToClient(Long userId, String notificationJson) throws ExecutionException, InterruptedException {
        String token = userRepository.findByUuid(userId)
                .orElseThrow(() -> {
                    log.error("FCM token not found for userId: {}", userId);
                    return new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN);
                }).getFcmToken();

        if (token == null || token.isEmpty()) {
            log.error("FCM token is null or empty for userId: {}", userId);
            throw new FcmException(ExceptionCode.NOT_FOUND_FCM_TOKEN);
        }

        log.info("Retrieved FCM token for userId: {}", userId);

        Map<String, String> data = new HashMap<>();
        data.put("message", notificationJson);

        log.debug("FCM message payload: {}", data);

        Message firebaseMessage = Message.builder()
                .setToken(token)
                .setAndroidConfig(AndroidConfig.builder()
                        .setTtl(43200000)
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setTitle("새로운 메시지")
                                .setBody(notificationJson)
                                .build())
                        .putAllData(data)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().sendAsync(firebaseMessage).get();
            log.info("Successfully sent FCM message. Response: {}", response);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send FCM message for userId: {}. Error: {}",
                    userId, e.getMessage());
            throw e;
        }
    }

    // WebSocket 연결 종료 시 세션 ID 제거 및 사용자 상태를 "offline"으로 설정
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String userId = redisTemplate.opsForValue().get("session:user:" + sessionId);

        if (userId != null) {
            redisTemplate.opsForValue().set("user:status:" + userId, "offline");
            redisTemplate.delete("user:session:" + userId);
        }

        redisTemplate.delete("session:user:" + sessionId);
        redisTemplate.delete("session:status:" + sessionId);
        log.info("WebSocket 연결 종료: Session ID = {}", sessionId);
    }

    // 특정 세션이 접속 중인지 확인하는 메서드
    public boolean isSessionConnected(String sessionId) {
        return connectedSessions.contains(sessionId);
    }
}