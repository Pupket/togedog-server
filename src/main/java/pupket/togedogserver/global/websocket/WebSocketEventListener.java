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
    private static final ThreadLocal<String> currentSessionIdHolder = new ThreadLocal<>();


    // WebSocket 연결 시 세션 ID 저장 및 사용자 상태를 "online"으로 설정
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("WebSocket 연결됨: 세션 ID = {}", sessionId);

        // ThreadLocal에 현재 세션 ID 저장
        currentSessionIdHolder.set(sessionId);
        connectedSessions.add(sessionId);

    }

    // WebSocket 연결 종료 시 세션 ID 제거 및 사용자 상태를 "offline"으로 설정
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket 연결 종료: 세션 ID = {}", sessionId);

        // ThreadLocal에서 세션 ID 제거
        if (sessionId.equals(currentSessionIdHolder.get())) {
            currentSessionIdHolder.remove();
        }

        connectedSessions.remove(sessionId);
    }
    // 현재 세션 ID 가져오기
    public String getCurrentSessionId() {
        return currentSessionIdHolder.get(); // ThreadLocal에서 현재 세션 ID 반환
    }

    // 특정 세션이 접속 중인지 확인하는 메서드
    public boolean isSessionConnected(String sessionId) {
        return connectedSessions.contains(sessionId);
    }
}