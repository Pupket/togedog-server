package pupket.togedogserver.domain.dog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.service.DogServiceImpl;
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
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestPart(value = "request", required = true) @Valid DogRegistRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage

    ) {
        dogService.create(userDetail, request, profileImage);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "강아지 프로필 수정", description = "강아지 프로필을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 수정 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 수정 실패")
    })
    @PatchMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestPart @Valid DogUpdateRequest request,
            @RequestPart("multipartFile") MultipartFile profileImage

    ) {
        dogService.update(userDetail, request, profileImage);

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

    @Operation(summary = "강아지 프로필 삭제", description = "산책 메이트 프로필 정보를 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로필 삭제 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 삭제 실패")
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
