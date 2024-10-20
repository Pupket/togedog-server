package pupket.togedogserver.global.jwt.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pupket.togedogserver.global.jwt.service.JwtService;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtService jwtService;

    public void handleExpiredRefreshToken(String refreshToken) {

        jwtService.deleteRefreshTokenDB(refreshToken);

    }

}
