package pupket.togedogserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.dog.repository.CustomDogRepositoryImpl;
import pupket.togedogserver.domain.notification.service.FcmServiceImpl;
import pupket.togedogserver.domain.token.entity.RefreshToken;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.token.repository.SocialAccessTokenRepository;
import pupket.togedogserver.domain.user.controller.port.UserService;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.response.DogActiveResponse;
import pupket.togedogserver.domain.user.dto.response.FindMateAndDogResponse;
import pupket.togedogserver.domain.user.dto.response.FindUserInfoResponse;
import pupket.togedogserver.domain.user.dto.response.MateActiveResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.jpaRepository.UserJPARepository;
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
    private final FcmServiceImpl fcmServiceImpl;
    private final RedisLoginService redisLoginService;

    @Override
    public User create(CustomUserDetail userDetail, RegistMateRequest request) {
        log.info("사용자 생성 시작: 사용자 ID = {}", userDetail.getUuid());
        User user = getUserById(userDetail.getUuid());
        User createdUser = createUserByRequest(request, user);
        log.info("사용자 생성 완료: 사용자 ID = {}", createdUser.getUuid());
        return userRepository.save(createdUser);
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
        return passwordEncoder.encode(password);
    }

    @Override
    public void logout(String refreshToken, CustomUserDetail userDetail) {
        log.info("로그아웃 시작: 사용자 ID = {}", userDetail.getUuid());
        jwtUtils.handleExpiredRefreshToken(refreshToken);
        log.info("로그아웃 완료: 사용자 ID = {}", userDetail.getUuid());
    }

    @Override
    public JwtToken reissueToken(String refreshToken) {
        log.info("토큰 재발급 시작: 리프레시 토큰 = {}", refreshToken);
        JwtToken newToken = jwtService.reissueTokenByRefreshToken(refreshToken);
        log.info("토큰 재발급 완료: 새로운 액세스 토큰 = {}", newToken.getAccessToken());
        return newToken;
    }

    @Override
    public FindUserInfoResponse getMemberDetails(Long uuid) {
        log.info("회원 정보 조회 시작: 사용자 ID = {}", uuid);
        User user = getUserById(uuid);
        FindUserInfoResponse response = FindUserInfoResponse.from(user);
        log.info("회원 정보 조회 완료: 사용자 ID = {}", uuid);
        return response;
    }

    @Override
    public void deleteSocialMember(Long uuid) {
        log.info("소셜 회원 삭제 시작: 사용자 ID = {}", uuid);
        User findUser = getUserById(uuid);
        deleteTokenFromSocialAccessToken(findUser);
        userRepository.delete(findUser);
        log.info("소셜 회원 삭제 완료: 사용자 ID = {}", uuid);
    }

    private void deleteTokenFromSocialAccessToken(User findUser) {
        socialAccessTokenRepository.findByUser(findUser).ifPresent(accessToken -> {
            String socialAccessToken = accessToken.getSocialAccessToken();
            revokeSocialAccessToken(findUser, socialAccessToken);
            socialAccessTokenRepository.delete(accessToken);
        });
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
        log.info("리프레시 토큰 조회 시작: 사용자 ID = {}", uuid);
        RefreshToken refreshToken = refreshTokenRepository.getRefreshTokenByMemberId(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
        log.info("리프레시 토큰 조회 완료: 사용자 ID = {}", uuid);
        return refreshToken.getRefreshToken();
    }

    @Override
    public FindMateAndDogResponse findMateAndDogActive(CustomUserDetail userDetail) {
        log.info("메이트 및 반려견 활동 조회 시작: 사용자 ID = {}", userDetail.getUuid());
        User findUser = getUserById(userDetail.getUuid());

        if (findUser.getMate() == null) {
            throw new MemberException(ExceptionCode.MATE_NOT_REGIST);
        }

        MateActiveResponse mateActions = customMateRepositoryImpl.findMateActions(findUser.getMate().getMateUuid(), findUser);
        DogActiveResponse dogActions = customDogRepositoryImpl.findDogActions(findUser.getUuid());

        log.info("메이트 및 반려견 활동 조회 완료: 사용자 ID = {}", userDetail.getUuid());
        return FindMateAndDogResponse.from(mateActions, dogActions);
    }

    @Override
    public JwtToken reissueTokenWithValidation(String refreshTokenInRequest, String accessToken) {
        log.info("토큰 유효성 검사 및 재발급 시작: 리프레시 토큰 = {}, 액세스 토큰 = {}", refreshTokenInRequest, accessToken);
        validateTokens(refreshTokenInRequest, accessToken);
        Long userId = getUserIdFromAccessToken(accessToken);
        validateRefreshToken(refreshTokenInRequest, userId);
        JwtToken newToken = reissueAndSaveNewToken(userId);
        log.info("토큰 유효성 검사 및 재발급 완료: 새로운 액세스 토큰 = {}", newToken.getAccessToken());
        return newToken;
    }

    private void validateTokens(String refreshToken, String accessToken) {
        if (refreshToken == null) {
            throw new MemberException(ExceptionCode.NOT_FOUND_REFRESH_TOKEN);
        }
        if (accessToken == null) {
            throw new MemberException(ExceptionCode.NOT_FOUND_ACCESS_TOKEN);
        }
    }

    private Long getUserIdFromAccessToken(String accessToken) {
        Long userId = jwtService.getUserIdFromToken(accessToken);
        if (userId == null) {
            throw new MemberException(ExceptionCode.NOT_FOUND_MEMBER);
        }
        return userId;
    }

    private void validateRefreshToken(String refreshTokenInRequest, Long userId) {
        String refreshTokenInDB = getRefreshToken(userId);
        if (!refreshTokenInRequest.equals(refreshTokenInDB)) {
            throw new MemberException(ExceptionCode.INVALID_TOKEN);
        }
    }

    private JwtToken reissueAndSaveNewToken(Long userId) {
        JwtToken newToken = reissueToken(getRefreshToken(userId));
        redisLoginService.saveAccessToken(newToken.getAccessToken(), userId);
        return newToken;
    }

    private User getUserById(Long uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
    }
}
