package pupket.togedogserver.global.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pupket.togedogserver.domain.token.entity.SocialAccessToken;
import pupket.togedogserver.domain.token.repository.SocialAccessTokenRepository;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.mapper.UserMapper;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.auth.dto.OAuthAttributes;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.security.util.PasswordUtil;

import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final SocialAccessTokenRepository socialAccessTokenRepository;
    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        DefaultOAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();
        String socialAccessToken = userRequest.getAccessToken().getTokenValue();

        OAuthAttributes oAuth2Attribute = OAuthAttributes.of(registrationId, userNameAttributeName,
                oAuth2User.getAttributes(), socialAccessToken);

        // 각 플랫폼의 유저 정보를 공통화 처리해주는 부분
        Map<String, Object> memberAttribute = oAuth2Attribute.convertToMap();
        String email = (String) memberAttribute.get("email");
        RoleType memberRole = RoleType.of(registrationId);
        String name = (String) memberAttribute.get("name");
        String picture = memberAttribute.get("picture") != null ? (String) memberAttribute.get("picture") : null;
        String nickname = memberAttribute.get("nickname") != null ? (String) memberAttribute.get("nickname") : null;
        UserGender gender = memberAttribute.get("gender") != null ? UserGender.valueOf(((String) memberAttribute.get("gender")).toUpperCase()) : null;

        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    // SocialAccessToken 엔티티 업데이트 또는 생성 로직 수정
                    socialAccessTokenRepository.findByUser(existingUser).ifPresentOrElse(
                            existingToken -> {
                                existingToken.updateSocialAccessToken(socialAccessToken);
                                socialAccessTokenRepository.save(existingToken);
                            },
                            () -> socialAccessTokenRepository.save(SocialAccessToken.of(socialAccessToken, existingUser)
                            )
                    );
                    return existingUser;

                }).orElseGet(() -> {
                    User newUser = userMapper.toEntity(email, name, picture, memberRole, nickname, gender);
                    String tempPassword = passwordUtil.generateRandomPassword();
                    newUser.updatePassword(tempPassword);
                    userRepository.save(newUser);
                    socialAccessTokenRepository.save(SocialAccessToken.of(socialAccessToken, newUser)); // 새로운 Member에 대한 SocialAccessToken 저장
                    return newUser;
                });

        return new CustomUserDetail(
                user,
                Collections.singleton(new SimpleGrantedAuthority(RoleType.of(registrationId).name())),
                memberAttribute);
    }

}
