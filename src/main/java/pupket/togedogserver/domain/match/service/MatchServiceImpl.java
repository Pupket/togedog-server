package pupket.togedogserver.domain.match.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.match.repository.MatchRepository;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.repository.OwnerRepository;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.domain.user.repository.mateRepo.MateRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MatchingException;
import pupket.togedogserver.global.exception.customException.MateException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.exception.customException.OwnerException;
import pupket.togedogserver.global.security.CustomUserDetail;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final MateRepository mateRepository;

    @Override
    public void match(CustomUserDetail userDetail, String nickname) {

        //Owner
        User findUserByUserDetail = userRepository.findByEmail(userDetail.getUsername()).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
        Owner owner = ownerRepository.findByUser(findUserByUserDetail).orElseThrow(
                () -> new OwnerException(ExceptionCode.NOT_FOUND_OWNER)
        );

        //Mate
        User findUserByNickname = userRepository.findByNickname(nickname).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );

        Mate mate = mateRepository.findByUser(findUserByNickname).orElseThrow(
                () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
        );

        //Owner 와 Mate를 연결시켜줘야함
        Match match = Match.builder()
                .owner(owner)
                .mate(mate)
                .build();

        matchRepository.save(match);

    }

    public void matchSuccess(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(
                () -> new MatchingException(ExceptionCode.NOT_FOUND_MATCH)
        );
        Match updatedMatch = match.toBuilder()
                .matched(MatchStatus.MATCHED)
                .build();

        matchRepository.save(updatedMatch);
    }

    public void matchFail(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(
                () -> new MatchingException(ExceptionCode.NOT_FOUND_MATCH)
        );
        Match updatedMatch = match.toBuilder()
                .matched(MatchStatus.UNMATCHED)
                .build();

        matchRepository.save(updatedMatch);
    }
}
