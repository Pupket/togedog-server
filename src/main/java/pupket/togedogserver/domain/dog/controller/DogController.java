package pupket.togedogserver.domain.dog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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
            @ApiResponse(responseCode = "200", description = "프로필 등록 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 등록 실패")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @ModelAttribute @Valid DogRegistRequest request
    ) {
        dogService.create(userDetail, request, request.getProfileImage());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "강아지 프로필 수정", description = "강아지 프로필을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 수정 실패")
    })
    @PatchMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestPart(value = "request") @Valid DogUpdateRequest request
    ) {
        dogService.update(userDetail, request, request.getProfileImage());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "강아지 프로필 랜덤 조회", description = "한 개의 강아지 프로필을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 조회 실패")
    })
    @GetMapping("/random")
    public ResponseEntity<Page<DogResponse>> findRandom(
            @Parameter(description = "페이지 시작 번호(0부터 시작)")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 사이즈(5부터 시작)")
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<DogResponse> dogList = dogService.findRandom(pageable);

        return ResponseEntity.ok().body(dogList);
    }


    @Operation(summary = "강아지 프로필 단일 조회", description = "한 개의 강아지 프로필을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
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
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
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
            @ApiResponse(responseCode = "200", description = "프로필 삭제 성공",
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

    //TODO:: performance 보고 Async적용 여부 결정
    @Operation(summary = "견종 이름 자동완성", description = "입력할때마다 관련 키워드 제공")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 삭제 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 삭제 실패")
    })
    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<List<String>> autoCompleteKeyword(
            @PathVariable("keyword") String keyword
    ) {
        List<String> result = dogService.autoCompleteKeyword(keyword);

        return ResponseEntity.ok().body(result);
    }
}
