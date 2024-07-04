package pupket.togedogserver.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pupket.togedogserver.domain.user.dto.request.SignUpRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateRequest;
import pupket.togedogserver.domain.user.dto.response.FindUserResponse;
import pupket.togedogserver.domain.user.service.UserServiceImpl;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.security.util.CookieUtils;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class UserController {

    private final UserServiceImpl userServiceImpl;
    private final CookieUtils cookieUtils;

    @Operation(summary = "회원 가입", description = "소셜 회원가입 추가정보 기입",
            parameters = {
                    @Parameter(name = "nickname", description = "닉네임", example = "tester", required = true,
                            schema = @Schema(type = "string", implementation = String.class)),
                    @Parameter(name = "userGender", description = "성별",
                            examples = {
                                    @ExampleObject(name = "남성", value = "MALE"),
                                    @ExampleObject(name = "여성", value = "FEMALE")
                            },
                            required = true,
                            schema = @Schema(type = "string", implementation = String.class)),

                    @Parameter(name = "genderVisibility", description = "성별 표시 유무",
                            examples = {
                                    @ExampleObject(name = "표시", value = "ACTIVE"),
                                    @ExampleObject(name = "숨김", value = "HIDDEN"),
                            },
                            required = true,
                            schema = @Schema(type = "string", implementation = String.class)),
                    @Parameter(name = "address1", description = "주소1", example = "전라남도 목포시 옥암동 제일 6차", required = true,
                            schema = @Schema(type = "string", implementation = String.class)),
                    @Parameter(name = "address2", description = "주소2", example = "1234동 496호", required = true,
                            schema = @Schema(type = "string", implementation = String.class)),
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원 가입 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "회원 가입 실패 (필수 입력값을 입력하지 않은 경우, 비밀번호와 비밀번호 확인이 일치하지 않는 경우)")
    })
    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@AuthenticationPrincipal CustomUserDetail userDetail, @Validated @RequestBody SignUpRequest request) {
        userServiceImpl.create(userDetail, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "회원 정보 조회", description = "인증 토큰을 사용하여 회원 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<FindUserResponse> find(@AuthenticationPrincipal CustomUserDetail user) {
        FindUserResponse updateUser = userServiceImpl.getMemberDetails(user.getUuid());
        return ResponseEntity.status(HttpStatus.OK).body(updateUser);
    }

    @Operation(summary = "회원 정보 수정", description = "회원 정보를 수정합니다.",
            parameters = {
                    @Parameter(name = "nickname", description = "닉네임", example = "new Tester", required = true, schema = @Schema(type = "String", implementation = String.class)),
                    @Parameter(name = "genderVisibility", description = "성별 표시 유무", example = "ACTIVE", schema = @Schema(type = "String", implementation = String.class)),
                    @Parameter(name = "address1", description = "메인 주소", example = "전라남도 목포시 옥암동 제일6차아파트", schema = @Schema(type = "String", implementation = String.class)),
                    @Parameter(name = "address2", description = "메인 주소", example = "전라남도 목포시 옥암동 제일6차아파트", schema = @Schema(type = "String", implementation = String.class)),
                    @Parameter(name = "mapX", description = "mapX", example = "123.45677", schema = @Schema(type = "Int", implementation = Integer.class)),
                    @Parameter(name = "mapY", description = "mapY", example = "123.45677", schema = @Schema(type = "Int", implementation = Integer.class))
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 수정 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "401", description = "로그인 실패: 인증에 실패하였습니다.")
    })
    @PatchMapping
    public ResponseEntity<Void> update(@AuthenticationPrincipal CustomUserDetail user, @RequestBody UpdateRequest updateReq) {
        userServiceImpl.updateMember(user, updateReq);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @Operation(summary = "로그아웃", description = "쿠키에 저장된 리프레쉬 토큰을 사용하여 로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "리프레시 토큰이 쿠키에 없습니다.")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = cookieUtils.getRefreshToken(request);

        userServiceImpl.logout(refreshToken, response);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "토큰 재발급", description = "accessToken을 재발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "발급 실패")
    })
    @GetMapping("/reissue-token")
    public ResponseEntity<String> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = cookieUtils.getRefreshToken(request);
        JwtToken newToken = userServiceImpl.reissueToken(refreshToken);
        cookieUtils.addCookie(response, "refreshToken", newToken.getRefreshToken(), 24 * 60 * 60 * 7);
        return ResponseEntity.status(HttpStatus.OK).body(newToken.getAccessToken());
    }

    @Operation(summary = "소셜 회원 탈퇴", description = "소셜 회원은 재로그인을 통해 검증, 재발급 받은 액세스 토큰을 통해 서비스 탈퇴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "소셜 회원 탈퇴 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "해당 소셜 회원이 존재하지 않습니다.")
    })
    @DeleteMapping("/social/me")
    public ResponseEntity<Void> deleteSocialMember(@AuthenticationPrincipal CustomUserDetail user) {
        userServiceImpl.deleteSocialMember(user.getUuid());

        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
