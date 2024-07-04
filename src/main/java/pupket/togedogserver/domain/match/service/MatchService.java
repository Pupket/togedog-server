package pupket.togedogserver.domain.match.service;


import pupket.togedogserver.global.security.CustomUserDetail;

public interface MatchService {

    void match(CustomUserDetail userDetail, String nickname);
}
