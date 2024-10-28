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
import pupket.togedogserver.global.redis.RedisLoginService;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtTokenProvider;
    private final RedisLoginService redisLoginService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();


        redisLoginService.saveAccessToken(jwtToken.getAccessToken(),userDetail.getUuid());


        String targetUrl = UriComponentsBuilder.fromUriString("togedog://togedog/login")
                .queryParam("accessToken", jwtToken.getAccessToken())
                .queryParam("refreshToken",jwtToken.getRefreshToken())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
