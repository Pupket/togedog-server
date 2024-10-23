    package pupket.togedogserver.domain.chat.controller;

    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.responses.ApiResponses;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
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
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Objects;

    @RestController
    @RequestMapping("/api/v1/chat")
    @RequiredArgsConstructor
    @Slf4j
    public class ChatController {

        private final ChatService chatService;
        private final S3FileUtilImpl s3FileUtilImpl;
        private final RedisPublisher redisPublisher;
        private final Map<String, ChannelTopic> topics = new HashMap<>();

        @MessageMapping("/chat")
        public void message(@Payload ChattingRequestDto message){
            log.info("Received Message : {} " , message);

            ChannelTopic topic = topics.get(String.valueOf(message.getRoomId()));
            if(topic == null){
                log.warn("Topic not found for roomId={}", message.getRoomId());
            }

            Timestamp parsedLastTime = chatService.getParsedLastTime(message.getLastTime());
            // 메시지를 Redis에 저장하기 위한 로직 추가
            ChattingResponseDto responseDto = new ChattingResponseDto(
                    parsedLastTime,
                    message.getRoomId(),
                    message.getUserId(),
                    message.getContent(),
                    message.getImage()
            );
            chatService.saveChatToRedis(String.valueOf(message.getRoomId()), responseDto);

            redisPublisher.publish(responseDto);
        }

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

            Long roomId = chatService.createChatRoom(userDetail.getUuid(), receiver);

            topics.put(String.valueOf(roomId), new ChannelTopic("/sub/chat/room/" + roomId)); // Add room topic
            log.info(topics.toString());
            return ResponseEntity.ok(roomId);
        }

        @Operation(summary = "채팅방 목록 반환",
                description = "사용자가 포함된 모든 채팅방 목록을 반환합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "채팅방 목록 가져오기 성공"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "500", description = "서버 오류")
        })
        @GetMapping("/chatroom-list")
        public ResponseEntity<List<ChatRoomResponseDto>> getChatRoomList(
                @AuthenticationPrincipal CustomUserDetail userDetail
        ) {
            return ResponseEntity.ok(chatService.getChatRoomList(userDetail.getUuid()));
        }

        @Operation(summary = "채팅 내역 백업",
                description = "사용자의 기기에 남아있던 모든 채팅 리스트를 백업합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "채팅 내역 백업 성공"),
                @ApiResponse(responseCode = "400", description = "처리할 수 없음")
        })
        @PostMapping("/backup")
        public void backupChats(
                @AuthenticationPrincipal CustomUserDetail userDetail,
                @RequestBody List<ChattingRequestDto> chats
        ) {
            chatService.backupChats(userDetail.getUuid(), chats);
        }

        @Operation(summary = "채팅방 나가기",
                description = "채팅방을 나갑니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "채팅방 삭제가 성공적으로 처리됨"),
                @ApiResponse(responseCode = "404", description = "요청한 채팅방의 정보를 찾을 수 없음")
        })
        @PostMapping("/leave")
        public void leaveRoom(Long roomId) {
            chatService.leaveRoom(roomId);
        }

        @Operation(summary = "이미지 업로드 및 url 반환",
                description = "이미지 url을 반환합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "성공"),
                @ApiResponse(responseCode = "404", description = "실패")
        })
        @PostMapping(value = "/get-imageUrl", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<String> getImageUrl(
                @RequestPart("image") MultipartFile image) {
            String uploadedImage = s3FileUtilImpl.upload(image);
            System.out.println(uploadedImage);
            return ResponseEntity.ok(uploadedImage);
        }


    }
