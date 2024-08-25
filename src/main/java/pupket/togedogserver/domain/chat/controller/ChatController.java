package pupket.togedogserver.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pupket.togedogserver.domain.user.service.UserServiceImpl;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.security.util.CookieUtils;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final UserServiceImpl userServiceImpl;
    private final CookieUtils cookieUtils;

    @CrossOrigin
    @GetMapping("/chat/{roomId}")
    public void chatConnect(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable String roomId
    ) {
        // TODO:: 클라이언트 채팅 페이지로 연결
    }

}
