package pupket.togedogserver.domain.match.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    //매칭요청
    @GetMapping()
    public ResponseEntity<Void> match(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestParam("nickname") String nickname
    ) {
        matchService.match(userDetail, nickname);

        return ResponseEntity.ok().build();
    }

    //매칭 수락
    @GetMapping("/{id}")
    public ResponseEntity<Void> matchingSuccess(@PathVariable("id") Long id) {

        matchService.matchSuccess(id);

        return ResponseEntity.ok().build();
    }


    //매칭 거절
    @PatchMapping()
    public ResponseEntity<Void> matchingFail(@PathVariable("id") Long id) {

        matchService.matchFail(id);

        return ResponseEntity.ok().build();

    }


}
