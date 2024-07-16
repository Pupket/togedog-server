package pupket.togedogserver.domain.dog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.service.DogServiceImpl;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dog")
@RequiredArgsConstructor
public class DogController {

    private final DogServiceImpl dogService;

    @Operation(summary = "강아지 프로필 등록", description = "강아지 프로필을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 등록 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 등록 실패")
    })
    @Parameters({
            @Parameter(name = "name", description = "강아지 이름", example = "Buddy", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "breed", description = "강아지 품종", example = "Golden Retriever", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "isNeuterized", description = "중성화 여부", example = "true", required = true, schema = @Schema(type = "boolean")),
            @Parameter(name = "dogGender", description = "강아지 성별 (true: 남, false: 여)", example = "true", required = true, schema = @Schema(type = "boolean")),
            @Parameter(name = "weight", description = "강아지 체중", example = "30", required = true, schema = @Schema(type = "integer")),
            @Parameter(name = "region", description = "지역 (SEOUL, INCHEON, GYEONGGI, CHUNGCHEONG, GYEONGSANG, JEOLLA, GANGWON, JEJU)", example = "SEOUL", required = true, schema = @Schema(implementation = Region.class)),
            @Parameter(name = "notes", description = "비고", example = "귀여운 강아지입니다. 슬개골이 약해요", required = false, schema = @Schema(type = "string")),
            @Parameter(name = "tag", description = "태그", example = "[\"친근한\", \"활발한\"]", required = false, schema = @Schema(type = "array", implementation = List.class)),
            @Parameter(name = "birthday", description = "생일", example = "2018-07-10", required = true, schema = @Schema(type = "string", format = "date"))
    })
    @PostMapping
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody @Valid DogRegistRequest request
    ) {
        dogService.create(userDetail, request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "강아지 프로필 수정", description = "강아지 프로필을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 수정 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 수정 실패")
    })
    @Parameters({
            @Parameter(name = "id", description = "강아지 프로필 ID, 화면에는 보이지 않아야 함", example = "1", required = true, schema = @Schema(type = "integer")),
            @Parameter(name = "name", description = "강아지 이름", example = "Buddy", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "breed", description = "강아지 품종", example = "Golden Retriever", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "isNeuterized", description = "중성화 여부", example = "true", required = true, schema = @Schema(type = "boolean")),
            @Parameter(name = "gender", description = "강아지 성별 (true: 남, false: 여)", example = "true", required = true, schema = @Schema(type = "boolean")),
            @Parameter(name = "weight", description = "강아지 체중", example = "30", required = true, schema = @Schema(type = "integer")),
            @Parameter(name = "region", description = "지역 (SEOUL, INCHEON, GYEONGGI, CHUNGCHEONG, GYEONGSANG, JEOLLA, GANGWON, JEJU)", example = "SEOUL", required = true, schema = @Schema(implementation = Region.class)),
            @Parameter(name = "note", description = "비고", example = "Very friendly", required = false, schema = @Schema(type = "string")),
            @Parameter(name = "tag", description = "태그", example = "[\"friendly\", \"energetic\"]", required = false, schema = @Schema(type = "array", implementation = List.class))
    })
    @PatchMapping
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody @Valid DogUpdateRequest request
    ) {
        dogService.update(userDetail, request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "강아지 프로필 단일 조회", description = "한 개의 강아지 프로필을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 조회 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 조회 실패")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DogResponse> find(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable Long id) {
        DogResponse dogResponse = dogService.find(userDetail, id);

        return ResponseEntity.ok().body(dogResponse);
    }

    @Operation(summary = "강아지 프로필 전체 조회", description = "강아지 프로필을 전체 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 조회 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 조회 실패")
    })
    @GetMapping
    public ResponseEntity<List<DogResponse>> findAll(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        List<DogResponse> dogList = dogService.findAll(userDetail);

        return ResponseEntity.ok().body(dogList);
    }

    @Operation(summary = "산책 메이트 프로필 등록", description = "산책 메이트 프로필 정보를 등록합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 등록 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 등록 실패")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable Long id
    ) {
        dogService.delete(userDetail, id);

        return ResponseEntity.ok().build();
    }
}
