package pupket.togedogserver.domain.match.controller.port;

import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.concurrent.ExecutionException;

public interface MatchService {

    void match(CustomUserDetail userDetail, String nickname, Long boardId);

    void matchSuccess(CustomUserDetail userDetail, Long boardId) ;

    void matchFail(CustomUserDetail userDetail, Long boardId);

    void completeWalking(Long boardId, CustomUserDetail userDetail);
}
