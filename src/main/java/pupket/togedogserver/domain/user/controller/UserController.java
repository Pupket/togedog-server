package pupket.togedogserver.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import pupket.togedogserver.domain.user.controller.port.UserService;
import pupket.togedogserver.domain.user.dto.response.FindMateAndDogResponse;
import pupket.togedogserver.domain.user.dto.response.FindUserInfoResponse;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.security.CustomUserDetail;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @Operation(summary = "회원 정보 조회", description = "인증 토큰을 사용하여 회원 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "회원 정보를 찾을 수 없음 또는 접근 권한 없음")
    })
    @GetMapping
    public ResponseEntity<FindUserInfoResponse> find(
            @AuthenticationPrincipal CustomUserDetail user
    ) {
        FindUserInfoResponse updateUser = userService.getMemberDetails(user.getUuid());
        return ResponseEntity.status(HttpStatus.OK).body(updateUser);
    }

    @Operation(summary = "산책 메이트 및 반려견 데이터 조회", description = "산책 메이트 산책 시간 및 횟수와 반려견 산책 시간 및 횟수를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "데이터를 찾을 수 없음 또는 접근 권한 없음")
    })
    @GetMapping("/mate-dog-active")
    public ResponseEntity<FindMateAndDogResponse> findMateAndDogActive(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        FindMateAndDogResponse mateAndDogActives = userService.findMateAndDogActive(userDetail);

        return ResponseEntity.ok().body(mateAndDogActives);
    }

    @Operation(summary = "로그아웃", description = "DB에 저장된 리프레쉬 토큰을 사용하여 로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "리프레시 토큰을 찾을 수 없음 또는 접근 권한 없음")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        String refreshToken = userService.getRefreshToken(userDetail.getUuid());

        userService.logout(refreshToken, userDetail);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "토큰 재발급", description = "accessToken을 재발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "토큰을 찾을 수 없음 또는 접근 권한 없음")
    })
    @GetMapping("/reissue-token")
    @Transactional
    public ResponseEntity<String> reissue(
            HttpServletRequest request
    ) {

        String refreshTokenInRequest = request.getHeader("refresh-token");
        String accessToken = jwtService.resolveToken(request);

        // Service로 토큰 재발급 로직 위임
        JwtToken newToken = userService.reissueTokenWithValidation(refreshTokenInRequest, accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add("accessToken", newToken.getAccessToken());
        headers.add("refreshToken", newToken.getRefreshToken());
        
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }

    @Operation(summary = "소셜 회원 탈퇴", description = "소셜 회원은 재로그인을 통해 검증, 재발급 받은 액세스 토큰을 통해 서비스 탈퇴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "소셜 회원 탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "소셜 회원을 찾을 수 없음 또는 접근 권한 없음")
    })
    @DeleteMapping("/social/me")
    public ResponseEntity<Void> deleteSocialMember(
            @AuthenticationPrincipal CustomUserDetail user
    ) {
        userService.deleteSocialMember(user.getUuid());

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
