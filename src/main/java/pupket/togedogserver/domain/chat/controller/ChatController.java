package pupket.togedogserver.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pupket.togedogserver.domain.chat.entity.Chatting;
import pupket.togedogserver.domain.chat.service.ChatService;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @CrossOrigin
    @Operation(summary = "채팅방 연결", description = "특정 채팅방의 채팅 기록을 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅 기록 가져오기 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Chatting.class)
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/chat/{roomId}")
    public List<Chatting> chatConnect(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable Long roomId
    ) {
        return chatService.getChats(userDetail, roomId);
    }

}
