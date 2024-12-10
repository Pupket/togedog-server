package pupket.togedogserver.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.redis.RedisLoginService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DuplicateLoginFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisLoginService redisService;
    private static final List<String> EXCLUDE_URLS = List.of(
            "/css", "/swagger", "/v3/api-docs", "/login", "/favicon","/api/v1/member/reissue-token"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 특정 경로에 대해 필터링을 제외
        if (isExcludedUrl(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청에서 JWT 토큰을 가져옴
        String token = jwtService.resolveToken(request);
        if (token != null && jwtService.validateToken(token)) {
            Long userId = jwtService.getUserIdFromToken(token);

            // Redis에서 저장된 사용자의 토큰을 가져옴
            String redisToken = redisService.getAccessToken(String.valueOf(userId));

            // Redis에 저장된 토큰과 요청의 토큰이 다르면 중복 로그인으로 간주
            if (redisToken != null && !redisToken.equals(token)) {
                throw new MemberException(ExceptionCode.DUPLICATE_LOGIN);
            }
        }


        filterChain.doFilter(request, response);
    }

    // 필터 제외 경로 확인
    private boolean isExcludedUrl(String requestURI) {
        return EXCLUDE_URLS.stream().anyMatch(url -> pathMatcher.match(url, requestURI));
    }

}