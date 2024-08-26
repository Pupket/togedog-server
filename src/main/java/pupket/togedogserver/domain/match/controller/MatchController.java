package pupket.togedogserver.domain.match.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pupket.togedogserver.domain.match.service.MatchServiceImpl;
import pupket.togedogserver.global.security.CustomUserDetail;

@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
//TODO:: 상대방에게 알림이 가도록 설정해야 함
public class MatchController {
    private final MatchServiceImpl matchService;
    @Operation(summary = "매칭 요청", description = "매칭을 신청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매칭 신청 완료",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "매칭 신청 실패")
    })
    @GetMapping("/{nickname}/{boardId}")
    public ResponseEntity<Void> match(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Parameter(description = "신청할 유저 닉네임") @PathVariable("nickname") String nickname,
            @Parameter(description = "신청한 게시판 글 id") @PathVariable("boardId") Long boardId
    ) {
        matchService.match(userDetail, nickname, boardId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매칭 수락", description = "매칭을 수락합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매칭 수락 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "매칭 수락 실패")
    })
    @GetMapping("accept/{boardId}")
    public ResponseEntity<Void> matchingSuccess(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Parameter(description = "요청 수락할 게시글 id") @PathVariable("boardId") Long boardId)
    {

        matchService.matchSuccess(userDetail,boardId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매칭 거절", description = "매칭을 거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매칭 거절 성공",
                    content = {@Content(schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "매칭 거절 실패")
    })
    @PatchMapping("reject/{boardId}")
    public ResponseEntity<Void> matchingFail(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Parameter(description = "요청 거절할 게시글 id") @PathVariable("boardId") Long boardId) {

        matchService.matchFail(userDetail, boardId);

        return ResponseEntity.ok().build();

    }

}
