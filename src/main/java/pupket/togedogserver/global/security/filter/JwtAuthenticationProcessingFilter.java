package pupket.togedogserver.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import pupket.togedogserver.global.exception.ExceptionResponse;
import pupket.togedogserver.global.exception.customException.JwtException;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.redis.RedisLoginService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisLoginService redisService;
    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}") String secretKey;
    @Value("${jwt.token.access-token-expiration-time}") long accessTokenExpirationTime;
    @Value("${jwt.token.refresh-token-expiration-time}") long refreshTokenExpirationTime;

    private static final List<String> EXCLUDE_URLS = List.of(
            "/css", "/swagger", "/v3/api-docs", "/login", "/favicon", "/api/v1/member/reissue-token"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 특정 경로에 대해 필터링 제외
        if (isExcludedUrl(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {

        // 요청에서 JWT 토큰을 가져옴
        String token = jwtService.resolveToken(request);
        if (token != null && jwtService.validateToken(token)) {
            Long userId = jwtService.getUserIdFromToken(token);

            // Redis에서 저장된 사용자의 토큰을 가져옴
            String redisToken = redisService.getAccessToken(String.valueOf(userId));

            log.info("레디스 토큰={}", redisToken);
            log.info("요청 토큰={}", token);

            // Redis에 저장된 토큰과 요청 토큰이 다르면 중복 로그인 처리
            if (redisToken != null && !redisToken.equals(token)) {
                log.info("중복 로그인 감지 - 예외 발생 처리");
                handleDuplicateLogin(response);
                return;
            }


                Authentication authentication = jwtService.getAuthenticationFromAccessToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

        }} catch (JwtException ex) {
            log.error("JWT 예외 처리: {}", ex.getMessage());
            handleJwtException(response, ex); // JWT 예외를 처리
            return;
        }

        // 필터 체인을 계속 진행
        filterChain.doFilter(request, response);
    }

    // 중복 로그인 응답 처리
    private void handleDuplicateLogin(HttpServletResponse response) throws IOException {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                "CONFLICT",
                HttpStatus.CONFLICT,
                HttpStatus.CONFLICT.value(),
                "중복 로그인이 감지되었습니다."
        );

        String responseBody = objectMapper.writeValueAsString(exceptionResponse);

        response.setStatus(HttpStatus.CONFLICT.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    }

    private void handleJwtException(HttpServletResponse response, JwtException ex) throws IOException {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                "UNAUTHORIZED",
                HttpStatus.UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage()
        );

        String responseBody = objectMapper.writeValueAsString(exceptionResponse);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    }

    // 필터 제외 경로 확인
    private boolean isExcludedUrl(String requestURI) {
        return EXCLUDE_URLS.stream().anyMatch(url -> pathMatcher.match(url, requestURI));
    }
}