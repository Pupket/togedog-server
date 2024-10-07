package pupket.togedogserver.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.redis.RedisLoginService;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IpValidationFilter extends OncePerRequestFilter {

    private final RedisLoginService redisLoginService;
    private final UserRepository userRepository;

    private static final List<String> EXCLUDE_URLS = List.of(
            "/health-check","/swagger", "/v3/api-docs", "/swagger-resources", "/webjars", "/login", "/favicon"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Bypass JWT authentication for Swagger and /login paths
        if (isExcludedPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
            User findUser = userRepository.findById(userDetail.getUuid()).orElseThrow(
                    () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
            );
            String ipAddress = request.getRemoteAddr();

            // 중복 로그인 검증
            redisLoginService.validateDuplicateLogin(findUser, ipAddress);
        }


        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String requestURI) {
        return EXCLUDE_URLS.stream().anyMatch(requestURI::startsWith);
    }

}
