package pupket.togedogserver.global.jwt.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.global.exception.customException.JwtException;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.security.util.CookieUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtils cookieUtils;

    public void handleExpiredRefreshToken(String refreshToken, HttpServletResponse response) {

        cookieUtils.deleteCookie(response, "refreshToken");
        jwtService.deleteRefreshTokenDB(refreshToken);

    }

    private boolean validateRefreshToken(String refreshToken) {
        try {
            jwtService.validateToken(refreshToken);
            return refreshTokenRepository.existsByRefreshToken(refreshToken);
        } catch (JwtException e) {
            return false;
        }
    }
}
