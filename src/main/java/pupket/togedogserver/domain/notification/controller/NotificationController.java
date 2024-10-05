package pupket.togedogserver.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.notification.service.FcmService;
import pupket.togedogserver.domain.notification.service.NotificationServiceImpl;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
@Tag(name = "fcm", description = "fcm 생성 및 활용 api")
public class NotificationController {

    private final NotificationServiceImpl notificationService;
    private final FcmService fcmService;

    @Operation(summary = "fcm 토큰 발급", description = "fcm 토큰 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 발급 성공",
                    content = @Content(schema = @Schema(type = "string", description = "FCM 토큰"))),
            @ApiResponse(responseCode = "400", description = "토큰 발급 실패")
    })
    @PostMapping("/create")
    public void createToken(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody String token
    ) {
        fcmService.createToken(
                userDetail.getUuid(),
                token
        );
    }

    @Operation(summary = "푸시 알림 전송", description = "수신자에게 푸시 알림 전송")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 전송 성공",
                    content = @Content(schema = @Schema(implementation = NotificationRequestDto.class))),
            @ApiResponse(responseCode = "400", description = "알림 전송 실패")
    })
    @PostMapping("/send")
    public void sendNotification(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody NotificationRequestDto notification
    ) throws ExecutionException, InterruptedException {
        notificationService.sendNotification(
                userDetail.getUuid(),
                notification
        );
    }

}
