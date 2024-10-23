package pupket.togedogserver.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;
import pupket.togedogserver.global.exception.customException.JwtException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final List<String> EXCLUDE_URLS = List.of(
            "/health-check","/swagger", "/v3/api-docs", "/swagger-resources", "/webjars", "/login", "/favicon","/ws","/websocket_test.html"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (!requestURI.startsWith("/health")) {
            log.info("request.URI = {}", requestURI);
        }

        if (isExcludedPath(requestURI)) {
            log.info("it's excluded path = {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token == null) {
            handleJwtException(response, new JwtException(ExceptionCode.NOT_FOUND_TOKEN));
            return;
        }

        try {
            if (jwtService.validateToken(token)) {
                Authentication authentication = jwtService.getAuthenticationFromAccessToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException e) {
            log.info("JWT Exception", e);
            handleJwtException(response, e);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetail userDetail) {
            if (userRepository.findByUuid(userDetail.getUuid()).isEmpty()) {
                handleJwtException(response, new MemberException(ExceptionCode.NOT_FOUND_MEMBER));
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String requestURI) {
        return EXCLUDE_URLS.stream().anyMatch(requestURI::startsWith);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (token.contains("&refreshToken=")) {
                // &refreshToken= 부분을 제외하고 accessToken만 추출
                token = token.split("&refreshToken=")[0];
            }
            log.info("token={}", token);
            return token;
        }
        return null;
    }

    private void handleJwtException(HttpServletResponse response, TogedogException e) throws IOException {
        response.setStatus(e.getExceptionCode().getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"status\":  \"%s\", \"code\": %d, \"message\": \"%s\"} ",
                e.getExceptionCode().getHttpStatus().name(),
                e.getExceptionCode().getCode(),
                e.getExceptionCode().getMessage()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
        response.getWriter().close();
    }
}


