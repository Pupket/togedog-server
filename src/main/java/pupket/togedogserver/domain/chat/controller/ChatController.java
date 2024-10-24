package pupket.togedogserver.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.chat.dto.ChatRoomResponseDto;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.service.ChatService;
import pupket.togedogserver.domain.chat.service.RedisPublisher;
import pupket.togedogserver.global.s3.util.S3FileUtilImpl;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final S3FileUtilImpl s3FileUtilImpl;
    private final RedisPublisher redisPublisher;
    private final RedisTemplate<String, ChannelTopic> redisTopicTemplate;

    @MessageMapping("/chat")
    public void message(@Payload ChattingRequestDto message) {
        log.info("Received Message : {} ", message);

        Timestamp parsedLastTime = chatService.getParsedLastTime(message.getLastTime());

        // ChattingRequestDto -> ChattingResponseDto로 변환하여 사용
        ChattingResponseDto responseDto = ChattingResponseDto.builder()
                .lastTime(parsedLastTime)
                .roomId(message.getRoomId())
                .userId(message.getUserId())
                .content(message.getContent())
                .image(message.getImage())
                .build();

        // Redis에 메시지 저장
        chatService.saveChatToRedis(String.valueOf(message.getRoomId()), responseDto);

        // 메시지 발행
        redisPublisher.publish(responseDto);
    }

    @CrossOrigin
    @Operation(summary = "미수신 메시지 조회", description = "주어진 lastTime 이후의 메시지들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미수신 메시지 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/get-unreceived-messages")
    public ResponseEntity<List<ChattingResponseDto>> getUnreceivedMessages(
            @RequestParam Long roomId,
            @RequestParam String lastTime // 마지막으로 받은 메시지 시간
    ) {
        // 마지막으로 받은 시간 이후의 메시지를 조회하는 서비스 호출
        Timestamp parsedLastTime = chatService.getParsedLastTime(lastTime);
        List<ChattingResponseDto> unreceivedMessages = chatService.getMessagesAfterLastTime(roomId, parsedLastTime);

        return ResponseEntity.ok(unreceivedMessages);
    }

    @PostMapping("/get-or-create")
    public ResponseEntity<Long> getOrCreateChatRoom(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            Long receiver
    ) {
        Long roomId = chatService.getOrCreateChatRoom(userDetail.getUuid(), receiver).getRoomId();
        return ResponseEntity.ok(roomId);
    }

    @GetMapping("/chatroom-list")
    public ResponseEntity<List<ChatRoomResponseDto>> getChatRoomList(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        return ResponseEntity.ok(chatService.getChatRoomList(userDetail.getUuid()));
    }

    @PostMapping("/leave")
    public void leaveRoom(Long roomId) {
        chatService.leaveRoom(roomId);
    }

    @PostMapping(value = "/get-imageUrl", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> getImageUrl(
            @RequestPart("image") MultipartFile image) {
        String uploadedImage = s3FileUtilImpl.upload(image);
        return ResponseEntity.ok(uploadedImage);
    }
}
