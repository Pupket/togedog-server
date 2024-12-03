package pupket.togedogserver.domain.user.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.dog.repository.CustomDogRepositoryImpl;
import pupket.togedogserver.domain.notification.service.FcmServiceImpl;
import pupket.togedogserver.domain.token.entity.RefreshToken;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.token.repository.SocialAccessTokenRepository;
import pupket.togedogserver.domain.user.constant.*;
import pupket.togedogserver.domain.user.controller.port.UserService;
import pupket.togedogserver.domain.user.dto.request.Preferred;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.response.DogActiveResponse;
import pupket.togedogserver.domain.user.dto.response.FindMateAndDogResponse;
import pupket.togedogserver.domain.user.dto.response.FindUserInfoResponse;
import pupket.togedogserver.domain.user.dto.response.MateActiveResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.repository.jpaRepository.UserJPARepository;
import pupket.togedogserver.domain.user.service.port.CustomMateRepository;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.auth.service.OAuth2RevokeService;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.jwt.util.JwtUtils;
import pupket.togedogserver.global.redis.RedisLoginService;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserJPARepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private SocialAccessTokenRepository socialAccessTokenRepository;
    @Mock
    private CustomMateRepository customMateRepositoryImpl;
    @Mock
    private CustomDogRepositoryImpl customDogRepositoryImpl;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OAuth2RevokeService oAuth2RevokeService;
    @Mock
    private FcmServiceImpl fcmServiceImpl;
    @Mock
    private RedisLoginService redisLoginService;

    private CustomUserDetail userDetail;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .uuid(1L)
                .email("tjsfdh@naver.com")
                .password(passwordEncoder.encode("test"))
                .nickname("tester")
                .role(RoleType.MEMBER_KAKAO)
                .build();
        userDetail = new CustomUserDetail(
                "testUser",
                "password",
                1L,
                Collections.singletonList(new SimpleGrantedAuthority(RoleType.MEMBER_KAKAO.name()))
        );
    }

    @Test
    @DisplayName("유저 생성")
    public void 유저_생성() throws Exception {
        // Given
        Preferred preferred = new Preferred();
        preferred.setRegion(Region.SEOUL);
        preferred.setDogTypes(Set.of(DogType.MID, DogType.BIG));
        preferred.setTimes(Set.of(Time.MORNING, Time.AFTERNOON));
        preferred.setWeeks(Set.of(Week.TUE, Week.FRI));
        preferred.setHashTag(Set.of("친절한", "활발한"));

        RegistMateRequest registMateRequest = RegistMateRequest.builder()
            .nickname("tester")
            .userGender(UserGender.MALE)
            .phoneNumber("010223123")
            .birthday(String.valueOf(19900101))
            .career("테스트 경력")
            .preferredDetails(preferred)
            .build();

        // When
        when(userRepository.findByUuid(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.create(userDetail, registMateRequest);

        // Then
        assertThat(createdUser.getNickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void 로그아웃_성공() {
        // Given
        String refreshToken = "test-refresh-token";

        // When
        doNothing().when(jwtUtils).handleExpiredRefreshToken(refreshToken);
        doNothing().when(fcmServiceImpl).deleteToken(userDetail.getUuid());

        // Then
        assertThatCode(() -> userService.logout(refreshToken, userDetail))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void 토큰_재발급_성공() {
        // Given
        String refreshToken = "test-refresh-token";
        JwtToken expectedToken = JwtToken.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .build();

        // When
        when(jwtService.reissueTokenByRefreshToken(refreshToken)).thenReturn(expectedToken);

        // Then
        JwtToken result = userService.reissueToken(refreshToken);
        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void 회원_정보_조회_성공() {
        // Given
        Long uuid = 1L;

        // When
        when(userRepository.findByUuid(uuid)).thenReturn(Optional.of(user));

        // Then
        FindUserInfoResponse response = userService.getMemberDetails(uuid);
        assertThat(response.getName()).isEqualTo(user.getName());
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
    void 회원_정보_조회_실패() {
        // Given
        Long uuid = 999L;

        // When
        when(userRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userService.getMemberDetails(uuid))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining("Member not found.");
    }

    @Test
    @DisplayName("소셜 회원 삭제 성공")
    void 소셜_회원_삭제_성공() {
        // Given
        Long uuid = 1L;

        // When
        when(userRepository.findByUuid(uuid)).thenReturn(Optional.of(user));
        when(socialAccessTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        // Then
        assertThatCode(() -> userService.deleteSocialMember(uuid))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("리프레시 토큰 조회 성공")
    void 리프레시_토큰_조회_성공() {
        // Given
        Long uuid = 1L;
        RefreshToken refreshToken = RefreshToken.builder()
            .refreshToken("test-refresh-token")
            .memberId(uuid)
            .build();

        // When
        when(refreshTokenRepository.getRefreshTokenByMemberId(uuid))
                .thenReturn(Optional.of(refreshToken));

        // Then
        String result = userService.getRefreshToken(uuid);
        assertThat(result).isEqualTo(refreshToken.getRefreshToken());
    }

    @Test
    @DisplayName("메이트 및 반려견 활동 조회 성공")
    void 메이트_및_반려견_활동_조회_성공() {
        // Given
        User mockUser = mock(User.class);
        Mate mate = Mate.builder()
            .mateUuid(1L)
            .user(mockUser)
            .matchCount(0L)
            .career("테스트 경력")
            .accommodatableDogsCount(2)
            .preferredRegion(Region.SEOUL)
            .deleted(false)
            .build();

        when(userRepository.findByUuid(userDetail.getUuid())).thenReturn(Optional.of(mockUser));
        when(mockUser.getMate()).thenReturn(mate);

        MateActiveResponse mateResponse = mock(MateActiveResponse.class);
        DogActiveResponse dogResponse = mock(DogActiveResponse.class);

        when(customMateRepositoryImpl.findMateActions(any(), any())).thenReturn(mateResponse);
        when(customDogRepositoryImpl.findDogActions(any())).thenReturn(dogResponse);

        // When
        FindMateAndDogResponse response = userService.findMateAndDogActive(userDetail);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("토큰 재발급 및 검증 성공")
    void 토큰_재발급_및_검증_성공() {
        // Given
        String refreshToken = "test-refresh-token";
        String accessToken = "test-access-token";
        Long userId = 1L;
        JwtToken newToken = JwtToken.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .build();

        // When
        when(jwtService.getUserIdFromToken(accessToken)).thenReturn(userId);
        when(refreshTokenRepository.getRefreshTokenByMemberId(userId))
                .thenReturn(Optional.of(RefreshToken.builder()
                    .refreshToken(refreshToken)
                    .memberId(userId)
                    .build()));
        when(jwtService.reissueTokenByRefreshToken(refreshToken)).thenReturn(newToken);

        // Then
        JwtToken result = userService.reissueTokenWithValidation(refreshToken, accessToken);
        assertThat(result).isEqualTo(newToken);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 없음")
    void 토큰_재발급_실패_리프레시토큰_없음() {
        // Given
        String accessToken = "test-access-token";

        // Then
        assertThatThrownBy(() ->
                userService.reissueTokenWithValidation(null, accessToken))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining("Refresh token not found for the user.");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 액세스 토큰 없음")
    void 토큰_재발급_실패_액세스토큰_없음() {
        // Given
        String refreshToken = "test-refresh-token";

        // Then
        assertThatThrownBy(() ->
                userService.reissueTokenWithValidation(refreshToken, null))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining("Not Found Access Token");
    }




}