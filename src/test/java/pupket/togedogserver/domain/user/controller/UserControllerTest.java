package pupket.togedogserver.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import pupket.togedogserver.domain.user.controller.port.UserService;
import pupket.togedogserver.domain.user.dto.response.FindMateAndDogResponse;
import pupket.togedogserver.domain.user.dto.response.FindUserInfoResponse;
import pupket.togedogserver.domain.user.dto.response.DogActiveResponse;
import pupket.togedogserver.domain.user.dto.response.MateActiveResponse;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.security.CustomUserDetail;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;
    
    @Mock
    private JwtService jwtService;
    
    @InjectMocks
    private UserController userController;

    private CustomUserDetail userDetail;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        userDetail = new CustomUserDetail(
            "testUser",
            "password",
            1L,
            Collections.singletonList(new SimpleGrantedAuthority(RoleType.MEMBER_KAKAO.name()))
        );

        request = new MockHttpServletRequest();
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void find_Success() {
        // Given
        FindUserInfoResponse expectedResponse = FindUserInfoResponse.builder()
            .uuid(1L)
            .email("test@test.com")
            .name("tester")
            .build();
        
        when(userService.getMemberDetails(anyLong())).thenReturn(expectedResponse);

        // When
        ResponseEntity<FindUserInfoResponse> response = userController.find(userDetail);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isEqualTo(1L);
    }

    @Test
    @DisplayName("메이트 및 반려견 활동 조회 성공")
    void findMateAndDogActive_Success() {
        // Given
        MateActiveResponse mateResponse = MateActiveResponse.builder()
            .walkCount(5L)
            .build();
            
        DogActiveResponse dogResponse = DogActiveResponse.builder()
            .name("테스트견")
            .walkTime("2시간")
            .walkCount(10L)
            .build();
            
        FindMateAndDogResponse expectedResponse = FindMateAndDogResponse.builder()
            .mateActiveResponse(mateResponse)
            .dogActiveResponse(dogResponse)
            .build();
        
        when(userService.findMateAndDogActive(any())).thenReturn(expectedResponse);

        // When
        ResponseEntity<FindMateAndDogResponse> response = userController.findMateAndDogActive(userDetail);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // Given
        String refreshToken = "test-refresh-token";
        when(userService.getRefreshToken(anyLong())).thenReturn(refreshToken);
        doNothing().when(userService).logout(any(), any());

        // When
        ResponseEntity<Void> response = userController.logout(userDetail);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_Success() {
        // Given
        String refreshToken = "test-refresh-token";
        String accessToken = "test-access-token";
        request.addHeader("refresh-token", refreshToken);
        
        JwtToken newToken = JwtToken.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .build();
            
        when(jwtService.resolveToken(any())).thenReturn(accessToken);
        when(userService.reissueTokenWithValidation(any(), any())).thenReturn(newToken);

        // When
        ResponseEntity<String> response = userController.reissue(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get("accessToken")).contains(newToken.getAccessToken());
        assertThat(response.getHeaders().get("refreshToken")).contains(newToken.getRefreshToken());
    }

    @Test
    @DisplayName("소셜 회원 탈퇴 성공")
    void deleteSocialMember_Success() {
        // Given
        doNothing().when(userService).deleteSocialMember(anyLong());

        // When
        ResponseEntity<Void> response = userController.deleteSocialMember(userDetail);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}