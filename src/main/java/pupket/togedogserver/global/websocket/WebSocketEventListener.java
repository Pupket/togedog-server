package pupket.togedogserver.global.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.security.CustomUserDetail;

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
    private final JwtService jwtService;

    // WebSocket 연결 시 세션 ID 저장 및 사용자 상태를 "online"으로 설정
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String token = headerAccessor.getFirstNativeHeader("Authorization");
        log.info("WebSocket 연결 이벤트 수신: Session ID = {}, Authorization 헤더 = {}", sessionId, token);

        // SecurityContext에서 인증 정보 가져오기
        try{
            Long userId = jwtService.getUserIdFromToken(token);

            log.info("WebSocket 연결: User ID = {}, Session ID = {}", userId, sessionId);

                // Redis에 사용자와 세션 매핑
                redisTemplate.opsForValue().set("user:session:" + userId, sessionId, 3, TimeUnit.DAYS);
                redisTemplate.opsForValue().set("session:user:" + sessionId, userId.toString(), 3, TimeUnit.DAYS);
                redisTemplate.opsForValue().set("session:status:" + sessionId, "online", 3, TimeUnit.DAYS);
                log.info("Redis에 사용자 매핑 완료: user:session:{} -> {}, session:user:{} -> {}", userId, sessionId, sessionId, userId);

        }catch (Exception e){
            log.error("WebSocket 연결 처리 중 예외 발생: Session ID = {}, Error = {}", sessionId, e.getMessage(), e);
        }

        log.info("WebSocket 연결됨: 세션 ID = {}", sessionId);
        connectedSessions.add(sessionId);
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