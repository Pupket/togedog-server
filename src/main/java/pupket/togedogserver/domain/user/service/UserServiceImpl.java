package pupket.togedogserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.notification.service.FcmService;
import pupket.togedogserver.domain.token.entity.RefreshToken;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.token.repository.SocialAccessTokenRepository;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindUserResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.mapper.UserMapper;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.auth.service.OAuth2RevokeService;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.jwt.util.JwtUtils;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.security.util.PasswordUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SocialAccessTokenRepository socialAccessTokenRepository;
    private final OAuth2RevokeService oAuth2RevokeService;
    private final FcmService fcmService;
    private final RefreshTokenRepository refreshTokenRepository;

    public void create(CustomUserDetail userDetail, RegistMateRequest request) {
        User user = getUserById(userDetail.getUuid());

        String password = PasswordUtil.generateRandomPassword();
        password = passwordEncoder.encode(password);
        User createdUser = user.toBuilder()
                .userGender(request.getUserGender())
                .genderVisibility(request.getGenderVisibility())
                .nickname(request.getNickname())
                .password(password)
                .build();

        userRepository.save(createdUser);
    }

    public void logout(String refreshToken, CustomUserDetail userDetail) {
        jwtUtils.handleExpiredRefreshToken(refreshToken);
        fcmService.deleteToken(userDetail.getUuid());
    }

    public JwtToken reissueToken(String refreshToken) {
        return jwtService.reissueTokenByRefreshToken(refreshToken);
    }


    private User getUserById(Long uuid) {
        refreshTokenRepository.getRefreshTokenByMemberId(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_REFRESH_TOKEN)
        );
        return userRepository.findByUuid(uuid).
                orElseThrow(
                        () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
                );
    }

    public FindUserResponse getMemberDetails(Long uuid) {
        User user = getUserById(uuid);


        return userMapper.of(user);
    }

    public void deleteSocialMember(Long uuid) {
        User findUser = getUserById(uuid);

        socialAccessTokenRepository.findByUser(findUser).ifPresent(
                accessToken -> {
                    String socialAccessToken = accessToken.getSocialAccessToken();
                    revokeSocialAccessToken(findUser, socialAccessToken);
                    socialAccessTokenRepository.delete(accessToken);
                }
        );

        userRepository.delete(findUser);
    }

    private void revokeSocialAccessToken(User findUser, String socialAccessToken) {
        switch (findUser.getRole()) {
            case MEMBER_KAKAO -> oAuth2RevokeService.revokeKakao(socialAccessToken);
            case MEMBER_GOOGLE -> oAuth2RevokeService.revokeGoogle(socialAccessToken);
            case MEMBER_NAVER -> oAuth2RevokeService.revokeNaver(socialAccessToken);
        }
    }

    public String getRefreshToken(Long uuid) {
        User findUser = getUserById(uuid);
        RefreshToken refreshToken = refreshTokenRepository.getRefreshTokenByMemberId(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
        return refreshToken.getRefreshToken();
    }
}
