package pupket.togedogserver.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.service.MateServiceImpl;
import pupket.togedogserver.global.security.CustomUserDetail;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/mate")
public class MateController {

    private final MateServiceImpl mateService;

    @Operation(summary = "산책 메이트 프로필 등록", description = "산책 메이트 프로필 정보를 등록합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 등록 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 등록 실패")
    })
    @PostMapping
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @Valid @RequestBody RegistMateRequest signUpRequest
    ) {

        mateService.create(userDetails, signUpRequest);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "산책 메이트 프로필 수정", description = "산책 메이트 프로필 정보를 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 수정 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 수정 실패")
    })
    @PatchMapping
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Valid @RequestBody UpdateMateRequest updateMateRequest
    ) {
        mateService.update(userDetail, updateMateRequest);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "산책 메이트 프로필 조회", description = "산책 메이트 프로필 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 조회 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 조회 실패")
    })
    @GetMapping
    public ResponseEntity<FindMateResponse> find(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        FindMateResponse findMateResponse = mateService.find(userDetail);


        return ResponseEntity.ok().body(findMateResponse);
    }

    @Operation(summary = "산책 메이트 프로필 삭제", description = "산책 메이트 프로필 정보를 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 삭제 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 삭제 실패")
    })
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        mateService.delete(userDetail);

        return ResponseEntity.ok().build();
    }
}