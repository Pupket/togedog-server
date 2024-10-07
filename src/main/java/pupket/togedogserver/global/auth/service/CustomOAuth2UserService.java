package pupket.togedogserver.global.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pupket.togedogserver.domain.token.entity.SocialAccessToken;
import pupket.togedogserver.domain.token.repository.SocialAccessTokenRepository;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.mapper.UserMapper;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.auth.dto.OAuthAttributes;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.redis.RedisLoginService;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.security.util.PasswordUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final SocialAccessTokenRepository socialAccessTokenRepository;
    private final UserMapper userMapper;
    private final RedisLoginService redisLoginService;

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

        Map<String, Object> memberAttribute = oAuth2Attribute.convertToMap();
        String email = (String) memberAttribute.get("email");
        RoleType memberRole = RoleType.of(registrationId);
        String name = (String) memberAttribute.get("name");
        String profileImage = memberAttribute.get("picture") != null ? (String) memberAttribute.get("picture") : null;
        String nickname = memberAttribute.get("nickname") != null ? (String) memberAttribute.get("nickname") : null;
        UserGender gender = memberAttribute.get("gender") != null ? UserGender.valueOf(((String) memberAttribute.get("gender")).toUpperCase()) : null;
        int birthday = memberAttribute.get("birthday") != null ? (int) memberAttribute.get("birthday") : 0;
        int birthyear = memberAttribute.get("birthyear") != null ? (int) memberAttribute.get("birthyear") : 0;

        //user IP 가져오기
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        log.info("server={}", Objects.requireNonNull(attributes).getRequest().getRemoteAddr());
        String remoteAddr;
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();
        remoteAddr = request.getHeader("X-Forwarded-For");
        if(remoteAddr==null){
            remoteAddr = request.getRemoteAddr();
        }

        String finalRemoteAddr = remoteAddr;
        log.info(finalRemoteAddr);
        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    if (existingUser.accountStatus.toString().equals("DELETED")) {
                        throw new MemberException(ExceptionCode.MEMBER_ALREADY_WITHDRAW);
                    }
                    // SocialAccessToken 엔티티 업데이트 또는 생성 로직 수정
                    redisLoginService.storeUserIPAddressInRedis(existingUser, finalRemoteAddr);
                    return existingUser;

                }).orElseGet(() -> {
                    User mappedUser = userMapper.toEntity(email, name, profileImage, memberRole, nickname, gender, birthday, birthyear);
                    String tempPassword = PasswordUtil.generateRandomPassword();
                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    String encodedPassword = passwordEncoder.encode(tempPassword);
                    User newUser = mappedUser.toBuilder()
                            .password(encodedPassword)
                            .build();
                    userRepository.save(newUser);
                    socialAccessTokenRepository.save(SocialAccessToken.of(socialAccessToken, newUser)); // 새로운 Member에 대한 SocialAccessToken 저장
                    redisLoginService.storeUserIPAddressInRedis(mappedUser,finalRemoteAddr);
                    return newUser;
                });



        return new CustomUserDetail(
                user,
                Collections.singleton(new SimpleGrantedAuthority(RoleType.of(registrationId).name())),
                memberAttribute);
    }

    private void validateAndupdateSocialAccessToken(User existingUser, String socialAccessToken) {
        socialAccessTokenRepository.findByUser(existingUser).ifPresentOrElse(
                existingToken -> {
                    existingToken.updateSocialAccessToken(socialAccessToken);
                    socialAccessTokenRepository.save(existingToken);
                },
                () -> socialAccessTokenRepository.save(SocialAccessToken.of(socialAccessToken, existingUser)
                )
        );
    }
}
