package pupket.togedogserver.global.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.security.util.CookieUtils;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtTokenProvider;
    private final CookieUtils cookieUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        String targetUrl = UriComponentsBuilder.fromUriString("togedog://togedog/login")
                .queryParam("accessToken", jwtToken.getAccessToken())
                .build().toUriString();

        String refreshToken = jwtToken.getRefreshToken();
        cookieUtils.addCookie(response, "refreshToken", refreshToken, 24 * 60 * 60 * 7); // 7일

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
