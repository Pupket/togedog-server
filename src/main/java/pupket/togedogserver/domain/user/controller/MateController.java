package pupket.togedogserver.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.service.BoardServiceImpl;
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
    private final BoardServiceImpl boardServiceImpl;

    @Operation(summary = "산책 메이트 프로필 등록", description = "산책 메이트 프로필 정보를 등록합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 등록 성공",
                    content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    })
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Valid @RequestPart(value ="Request" ) RegistMateRequest Request,
            @Schema(description = "프로필 이미지 파일", type = "string", format = "binary", nullable = true)
            @RequestPart(value = "profileImage",required = false) MultipartFile profileImage) {

        mateService.create(userDetail, Request, profileImage);

        return ResponseEntity.status(200).build();
    }

    @Operation(summary = "산책 메이트 프로필 수정", description = "산책 메이트 프로필 정보를 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공",
                    content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "400", description = "프로필 수정 실패")
    })
    @PatchMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Valid @RequestPart(value = "updateMateRequest") UpdateMateRequest updateMateRequest,
            @RequestPart(value = "profileImage",required = false) MultipartFile profileImage
    ) {
        mateService.update(userDetail, updateMateRequest,profileImage);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "산책 메이트 프로필 조회", description = "산책 메이트 프로필 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
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

    @Operation(summary = "산책 메이트 랜덤 반환", description = "산책 메이트 프로필을 랜덤으로 반환합니다. (페이지 시작 0부터, 사이즈 4부터 시작)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 랜덤 반환 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 랜덤 반환 실패")
    })

    @GetMapping("/random")
    public ResponseEntity<Page<FindMateResponse>> findRandom(
            @Parameter(description = "페이지 시작 번호(0부터 시작)")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 사이즈(4부터 시작)")
            @RequestParam(name = "size", defaultValue = "4") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FindMateResponse> mateList = mateService.findRandom(pageable);

        return ResponseEntity.ok().body(mateList);
    }

    @DeleteMapping()
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        mateService.delete(userDetail);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "산책 메이트 닉네임 중복 체크", description = "산책 메이트 닉네임 중복 여부 체크")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 중복체크 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "프로필 중복체크 실패")
    })
    @Parameter(name = "nickname", description = "닉네임", example = "surno123", required = true, schema = @Schema(type = "string"))
    @GetMapping("/{nickname}")
    public ResponseEntity<Boolean> checkNickName(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable("nickname")
            @NotBlank(message = "닉네임은 필수 입력 값입니다.")
            @Size(max = 10, message = "닉네임은 10자 이하로 입력해야 합니다.")
            @Pattern(regexp = "^[\\p{L}0-9]+$", message = "닉네임에는 특수 문자를 사용할 수 없습니다.")
            String nickname
    ) {
        boolean flag = mateService.checkNickname(userDetail, nickname);

        return ResponseEntity.ok().body(flag);
    }

    //TODO:: performance 보고 @Async적용 여부 결정
    @Operation(summary = "산책 메이트 닉네임 자동 완성", description = "산책 메이트 닉네임 자동 완성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "검색 실패")
    })
    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<List<String>> autoCompleteKeyword(
            @PathVariable("keyword") String keyword
    ) {
        List<String> result = mateService.autoCompleteKeyword(keyword);

        return ResponseEntity.ok().body(result);
    }

    @Operation(summary = "산책 메이트 내 일정 조회", description = "산책 메이트 내 일정 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "검색 실패")
    })
    @GetMapping("/mySchedule")
    public ResponseEntity<Page<BoardFindResponse>> findMySchedule(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Parameter(description = "페이지 시작 번호(0부터 시작)")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 사이즈(5부터 시작)")
            @RequestParam(name = "size", defaultValue = "5") int size,
            @Parameter(description = "첫 요청 여부")
            @RequestParam(name = "first", defaultValue = "false") boolean first
    ) {
        // 첫 요청인 경우 사이즈를 10으로 설정
        if (first) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);

        Page<BoardFindResponse> boardList = boardServiceImpl.findMySchedule(userDetail, pageable);

        return ResponseEntity.ok().body(boardList);
    }

}