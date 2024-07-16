package pupket.togedogserver.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/mate")
public class MateController {

    private final MateServiceImpl mateService;

    @Operation(summary = "산책 메이트 프로필 등록", description = "산책 메이트 프로필 정보를 등록합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 등록 성공",
                    content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    })
    @Parameters({
            @Parameter(name = "nickname", description = "닉네임", example = "surno123", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "profileImage", description = "프로필 이미지 URL", example = "string", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "userGender", description = "사용자 성별(MALE,FEMALE)", example = "MALE", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "genderVisibility", description = "성별 공개 여부(ACTIVE,HIDDEN)", example = "ACTIVE", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "phoneNumber", description = "전화번호(오직 숫자만)", example = "12341234", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "age", description = "나이", example = "31", required = true, schema = @Schema(type = "integer")),
            @Parameter(name = "accommodatableDogsCount", description = "수용 가능한 강아지 수", example = "1", required = true, schema = @Schema(type = "integer")),
            @Parameter(name = "career", description = "경력", example = "대형견 산책 경험 있음", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "preferredBreed", description = "선호하는 견종(SMALL, MID, BIG, SUPER)", example = "[\"MID\", \"SUPER\"]", required = true, schema = @Schema(type = "array", implementation = List.class)),
            @Parameter(name = "preferredStyle", description = "선호하는 스타일", example = "[\"귀여운\"]", required = true, schema = @Schema(type = "array", implementation = List.class)),
            @Parameter(name = "preferredWeek", description = "선호하는 요일(MON~SUN)", example = "[\"MON\",\"TUE\",\"WED\",]", required = true, schema = @Schema(type = "array", implementation = List.class)),
            @Parameter(name = "preferredTime", description = "선호하는 시간대( MORNING, LUNCH, AFTERNOON, EVENING, DAWN)", example = "[\"MORNING\",\"EVENING\"]", required = true, schema = @Schema(type = "array", implementation = List.class)),
            @Parameter(name = "region", description = "지역(  SEOUL, INCHEON, GYEONGGI, CHUNGCHEONG, GYEONGSANG, JEOLLA, GANGWON, JEJU)", example = "JEJU", required = true, schema = @Schema(type = "string"))
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
                    content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "400", description = "프로필 수정 실패")
    })
    @Parameters({
            @Parameter(name = "nickname", description = "닉네임", example = "sunro1234", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "profileImage", description = "프로필 이미지 URL", example = "string", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "userGender", description = "사용자 성별", example = "MALE", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "genderVisibility", description = "성별 공개 여부", example = "string", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "phoneNumber", description = "전화번호", example = "12341234", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "age", description = "나이", example = "31", required = true, schema = @Schema(type = "integer")),
            @Parameter(name = "accommodatableDogsCount", description = "수용 가능한 강아지 수", example = "12", required = true, schema = @Schema(type = "integer")),
            @Parameter(name = "career", description = "경력", example = "능수능란", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "preferredBreed", description = "선호하는 견종(SMALL, MID, BIG, SUPER)", example = "[\"MID\", \"SUPER\"]", required = true, schema = @Schema(type = "array", implementation = List.class)),
            @Parameter(name = "preferredStyle", description = "선호하는 스타일", example = "[\"귀여운\"]", required = true, schema = @Schema(type = "array", implementation = List.class)),
            @Parameter(name = "preferredWeek", description = "선호하는 요일(MON~SUN)", example = "[\"MON\",\"TUE\",\"WED\",]", required = true, schema = @Schema(type = "array", implementation = List.class)),
            @Parameter(name = "preferredTime", description = "선호하는 시간대( MORNING, LUNCH, AFTERNOON, EVENING, DAWN)", example = "[\"MORNING\",\"EVENING\"]", required = true, schema = @Schema(type = "array", implementation = List.class)),
            @Parameter(name = "region", description = "지역(  SEOUL, INCHEON, GYEONGGI, CHUNGCHEONG, GYEONGSANG, JEOLLA, GANGWON, JEJU)", example = "JEJU", required = true, schema = @Schema(type = "string"))
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