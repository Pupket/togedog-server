package pupket.togedogserver.domain.board.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pupket.togedogserver.domain.board.dto.request.BoardCreateRequest;
import pupket.togedogserver.domain.board.dto.request.BoardUpdateRequest;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.service.BoardServiceImpl;
import pupket.togedogserver.global.security.CustomUserDetail;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/board")
public class BoardController {

    private final BoardServiceImpl boardService;

    @Operation(summary = "산책 게시판 글 등록", description = "산책 게시판 글을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 등록 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "게시글 등록 실패")
    })
    @PostMapping
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody @Valid BoardCreateRequest boardCreateRequest
    ) {
        boardService.create(userDetail, boardCreateRequest);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "산책 게시판 글 조회", description = "산책 게시판 글을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "게시글 조회 실패")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BoardFindResponse> find(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable("id") Long id
    ) {
        BoardFindResponse boardFindResponse = boardService.find(userDetail, id);

        return ResponseEntity.ok().body(boardFindResponse);
    }

    @Operation(summary = "산책 게시판 글 랜덤 반환", description = "산책 게시판 글을 랜덤으로 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 랜덤 반환 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "게시글 랜덤 반환 실패")
    })
    @GetMapping("/random")
    public ResponseEntity<Page<BoardFindResponse>> findRandom(
            @Parameter(description = "페이지 시작 번호(0부터 시작)")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 사이즈(5부터 시작)")
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<BoardFindResponse> boardList = boardService.findRandom(pageable);

        return ResponseEntity.ok().body(boardList);
    }

    @Operation(summary = "산책 게시판 수정", description = "산책 게시판 글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "게시글 수정 실패")
    })
    @PatchMapping
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody BoardUpdateRequest boardUpdateRequest
    ) {
        boardService.update(userDetail, boardUpdateRequest);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "산책 게시판 삭제", description = "산책 게시판 글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "게시글 삭제 실패")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable("id") Long id
    ) {
        boardService.delete(userDetail, id);

        return ResponseEntity.ok().build();
    }
}
