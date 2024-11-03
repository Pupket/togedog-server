package pupket.togedogserver.global.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pupket.togedogserver.global.jwt.service.JwtService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final Set<String> connectedSessions = new HashSet<>();
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtTokenProvider;  // JWT 토큰 파싱을 위한 JwtTokenProvider

    // WebSocket 연결 시 세션 ID 저장 및 사용자 상태를 "online"으로 설정
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String token = headerAccessor.getFirstNativeHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // "Bearer " 제거
            Long userId = jwtTokenProvider.getUserIdFromToken(token); // JWT 토큰에서 사용자 ID 추출
            log.info("userId={}", userId);

            if (userId != null) {
                // Redis에 사용자 상태를 "online"으로 저장 (3분 만료 시간)
                redisTemplate.opsForValue().set("user:status:" + userId, "online", 3, TimeUnit.MINUTES);
                log.info("User {} is now online", userId);
            }
        }

        log.info("WebSocket 연결됨: 세션 ID = {}", sessionId);
        connectedSessions.add(sessionId);
    }

    // WebSocket 연결 종료 시 세션 ID 제거 및 사용자 상태를 "offline"으로 설정
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String token = headerAccessor.getFirstNativeHeader("Authorization");
        log.info("EventListener Token = {} " , token);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // "Bearer " 제거
            Long userId = jwtTokenProvider.getUserIdFromToken(token); // JWT 토큰에서 사용자 ID 추출

            if (userId != null) {
                // Redis에 사용자 상태를 "offline"으로 저장
                redisTemplate.opsForValue().set("user:status:" + userId, "offline");
                log.info("User {} is now offline", userId);
            }
        }

        log.info("WebSocket 연결 종료됨: 세션 ID = {}", sessionId);
        connectedSessions.remove(sessionId);
    }

    // 특정 세션이 접속 중인지 확인하는 메서드 (옵션)
    public boolean isSessionConnected(String sessionId) {
        return connectedSessions.contains(sessionId);
    }
}