package pupket.togedogserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.dog.repository.CustomDogRepositoryImpl;
import pupket.togedogserver.domain.notification.service.FcmService;
import pupket.togedogserver.domain.token.entity.RefreshToken;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.token.repository.SocialAccessTokenRepository;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.controller.port.UserService;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.response.DogActiveResponse;
import pupket.togedogserver.domain.user.dto.response.FindMateAndDogResponse;
import pupket.togedogserver.domain.user.dto.response.FindUserInfoResponse;
import pupket.togedogserver.domain.user.dto.response.MateActiveResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.jpaRepository.UserJPARepository;
import pupket.togedogserver.domain.user.repository.CustomMateRepositoryImpl;
import pupket.togedogserver.domain.user.service.port.CustomMateRepository;
import pupket.togedogserver.global.auth.service.OAuth2RevokeService;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.jwt.util.JwtUtils;
import pupket.togedogserver.global.redis.RedisLoginService;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.security.util.PasswordUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserJPARepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SocialAccessTokenRepository socialAccessTokenRepository;
    private final CustomMateRepository customMateRepositoryImpl;
    private final CustomDogRepositoryImpl customDogRepositoryImpl;
    private final JwtUtils jwtUtils;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2RevokeService oAuth2RevokeService;
    private final FcmService fcmService;
    private final RedisLoginService redisLoginService;

    @Override
    public void create(CustomUserDetail userDetail, RegistMateRequest request) {
        User user = getUserById(userDetail.getUuid());

        User createdUser = createUserByRequest(request, user);

        userRepository.save(createdUser);
    }

    private User createUserByRequest(RegistMateRequest request, User user) {
        return user.toBuilder()
                .userGender(request.getUserGender())
                .nickname(request.getNickname())
                .password(createPassword())
                .build();
    }

    private String createPassword() {
        String password = PasswordUtil.generateRandomPassword();
        password = passwordEncoder.encode(password);
        return password;
    }

    @Override
    public void logout(String refreshToken, CustomUserDetail userDetail) {
        jwtUtils.handleExpiredRefreshToken(refreshToken);
        fcmService.deleteToken(userDetail.getUuid());
    }

    @Override
    public JwtToken reissueToken(String refreshToken) {
        return jwtService.reissueTokenByRefreshToken(refreshToken);
    }


    @Override
    public FindUserInfoResponse getMemberDetails(Long uuid) {
        User user = getUserById(uuid);

        return FindUserInfoResponse.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .name(user.getName())
                .platform(RoleType.toKoreanValue(user.getRole()))
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Override
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

    @Override
    public String getRefreshToken(Long uuid) {
        RefreshToken refreshToken = refreshTokenRepository.getRefreshTokenByMemberId(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
        return refreshToken.getRefreshToken();
    }

    @Override
    public FindMateAndDogResponse findMateAndDogActive(CustomUserDetail userDetail) {
        User findUser = getUserById(userDetail.getUuid());

        if (findUser.getMate() == null) {
            throw new MemberException(ExceptionCode.MATE_NOT_REGIST);
        }

        MateActiveResponse mateActions = customMateRepositoryImpl.findMateActions(findUser.getMate().getMateUuid(), findUser);
        DogActiveResponse dogActions = customDogRepositoryImpl.findDogActions(findUser.getUuid());

        return FindMateAndDogResponse.builder()
                .mateActiveResponse(mateActions)
                .dogActiveResponse(dogActions)
                .build();
    }

    @Override
    public JwtToken reissueTokenWithValidation(String refreshTokenInRequest, String accessToken) {
        // 1. 리프레시 토큰이 없으면 예외 발생
        if (refreshTokenInRequest == null) {
            throw new MemberException(ExceptionCode.NOT_FOUND_REFRESH_TOKEN);
        }
        // 2. 액세스 토큰이 없으면 예외 발생
        if (accessToken == null) {
            throw new MemberException(ExceptionCode.NOT_FOUND_ACCESS_TOKEN);
        }
        // 3. 액세스 토큰에서 유저 아이디 조회
        Long userId = jwtService.getUserIdFromToken(accessToken);
        if (userId == null) {
            throw new MemberException(ExceptionCode.NOT_FOUND_MEMBER);
        }

        String existingAccessToken = redisLoginService.getAccessToken(userId);
        if (existingAccessToken != null) {
            redisLoginService.deleteAccessToken(userId);
        }

        // 4. 유저 아이디로 리프레시 토큰 조회
        String refreshTokenInDB = getRefreshToken(userId);
        if (!refreshTokenInRequest.equals(refreshTokenInDB)) {
            throw new MemberException(ExceptionCode.INVALID_TOKEN);
        }

        // 5. 리프레시 토큰이 같으면 토큰 재발급
        JwtToken newToken = reissueToken(refreshTokenInDB);

        redisLoginService.saveAccessToken(newToken.getAccessToken(), userId);

        return newToken;
    }

    private User getUserById(Long uuid) {
        refreshTokenRepository.getRefreshTokenByMemberId(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.INVALID_TOKEN)
        );
        return userRepository.findByUuid(uuid).
                orElseThrow(
                        () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
                );
    }
}
