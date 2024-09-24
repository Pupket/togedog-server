package pupket.togedogserver.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.user.dto.response.FindMatchedScheduleResponse;
import pupket.togedogserver.domain.user.service.OwnerService;
import pupket.togedogserver.global.security.CustomUserDetail;

@RestController
@RequestMapping("api/v1/owner")
@RequiredArgsConstructor
@Slf4j
public class OwnerController {

    private final OwnerService ownerService;

    @Operation(summary = "내 산책 게시글 리스트 반환", description = "내 산책 게시글을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 반환 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "게시글 반환 실패")
    })
    @GetMapping("/myWalking")
    public ResponseEntity<Page<BoardFindResponse>> findMyBoards(
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

        Page<BoardFindResponse> boardList = ownerService.findMyBoards(userDetail, pageable);

        return ResponseEntity.ok().body(boardList);
    }

    @Operation(summary = "내 산책 일정 리스트 반환", description = "내 산책 일정을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "산책 일정 반환 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "산책 일정 반환 실패")
    })
    @GetMapping("/mySchedule")
    public ResponseEntity<Page<FindMatchedScheduleResponse>> findMySchedules(
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

        Page<FindMatchedScheduleResponse> boardList = ownerService.findMySchedule(userDetail, pageable);

        return ResponseEntity.ok().body(boardList);
    }
}
