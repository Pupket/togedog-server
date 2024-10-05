package pupket.togedogserver.domain.chat.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pupket.togedogserver.domain.chat.service.ChatService;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @CrossOrigin
    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성하고 채팅방 번호를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 생성"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/create")
    public ResponseEntity<Long> createChatRoom(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            Long receiver
    ) {
        return ResponseEntity.ok(chatService.createChatRoom(userDetail.getUuid(), receiver));
    }

    @Operation(summary = "채팅방 목록을 반환합니다.",
            description = "사용자가 포함된 모든 채팅방 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 목록 가져오기 성공"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/chatroom-list")
    public ResponseEntity<List<Long>> getChatRoomList(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        return ResponseEntity.ok(chatService.getChatRoomList(userDetail.getUuid()));
    }

    @Hidden
    @Operation(summary = "owner의 채팅방 목록을 반환합니다.",
            description = "owner 모드에 해당하는 채팅방 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 목록 가져오기 성공"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/owner-list")
    public ResponseEntity<List<Long>> getOwnerChatRoomList(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        return ResponseEntity.ok(chatService.getUser1ChatRoomList(userDetail.getUuid()));
    }

    @Hidden
    @Operation(summary = "mate의 채팅방 목록을 반환합니다.",
            description = "mate 모드에 해당하는 채팅방 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 목록 가져오기 성공"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/mate-list")
    public ResponseEntity<List<Long>> getMateChatRoomList(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        return ResponseEntity.ok(chatService.getUser2ChatRoomList(userDetail.getUuid()));
    }

}
