package pupket.togedogserver.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pupket.togedogserver.domain.notification.service.NotificationServiceImpl;
import pupket.togedogserver.domain.user.dto.response.FindUserResponse;
import pupket.togedogserver.domain.user.service.UserServiceImpl;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.security.util.CookieUtils;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class UserController {

    private final UserServiceImpl userServiceImpl;
    private final CookieUtils cookieUtils;
    private final NotificationServiceImpl notificationService;

    @Operation(summary = "회원 정보 조회", description = "인증 토큰을 사용하여 회원 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<FindUserResponse> find(
            @AuthenticationPrincipal CustomUserDetail user
    ) {
        FindUserResponse updateUser = userServiceImpl.getMemberDetails(user.getUuid());
        return ResponseEntity.status(HttpStatus.OK).body(updateUser);
    }

    @Operation(summary = "로그아웃", description = "DB에 저장된 리프레쉬 토큰을 사용하여 로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "리프레시 토큰이 쿠키에 없습니다.")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetail userDetail

    ) {

        String refreshToken = userServiceImpl.getRefreshToken(userDetail.getUuid());

        userServiceImpl.logout(refreshToken, userDetail);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "토큰 재발급", description = "accessToken을 재발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "발급 실패")
    })
    @GetMapping("/reissue-token")
    @Transactional
    public ResponseEntity<String> reissue(
            @AuthenticationPrincipal CustomUserDetail userDetail, HttpServletRequest request) {

        String refreshTokenInRequest = request.getHeader("Authorization");

        if (refreshTokenInRequest != null && refreshTokenInRequest.startsWith("Bearer ")) {
            refreshTokenInRequest = refreshTokenInRequest.substring(7); // "Bearer " 부분 제거
        }

        String refreshTokenInDB = userServiceImpl.getRefreshToken(userDetail.getUuid());
        JwtToken newToken = null;
        if (refreshTokenInRequest.equals(refreshTokenInDB)) {
            newToken = userServiceImpl.reissueToken(refreshTokenInDB);
        } else {
            throw new MemberException(ExceptionCode.INVALID_TOKEN);
        }
//        App에는 Cookie개념이 없기 때문에 사용하지 않음
//        cookieUtils.addCookie(response, "refreshToken", newToken.getRefreshToken(), 24 * 60 * 60 * 7);
        HttpHeaders headers = new HttpHeaders();
        headers.add("accessToken", newToken.getAccessToken());
        headers.add("refreshToken", newToken.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }

    @Operation(summary = "소셜 회원 탈퇴", description = "소셜 회원은 재로그인을 통해 검증, 재발급 받은 액세스 토큰을 통해 서비스 탈퇴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "소셜 회원 탈퇴 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "해당 소셜 회원이 존재하지 않습니다.")
    })
    @DeleteMapping("/social/me")
    public ResponseEntity<Void> deleteSocialMember(
            @AuthenticationPrincipal CustomUserDetail user
    ) {
        userServiceImpl.deleteSocialMember(user.getUuid());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
