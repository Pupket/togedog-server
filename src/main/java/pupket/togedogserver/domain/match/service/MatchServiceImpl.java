package pupket.togedogserver.domain.match.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.repository.BoardRepository;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
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
import pupket.togedogserver.global.exception.customException.*;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final MateRepository mateRepository;
    private final BoardRepository boardRepository;

    @Override
    public void match(CustomUserDetail userDetail, String nickname, Long boardId) {

        //Owner
        User findUserByUserDetail = userRepository.findByEmail(userDetail.getUsername()).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );

        if (findUserByUserDetail.getNickname().equals(nickname)) {
            throw new MemberException(ExceptionCode.YOUR_OWN_NICKNAME);
        }

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

        //Board
        Board findBoardById = boardRepository.findByBoardId(boardId).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );

        List<Match> matches = matchRepository.findByOwner(owner).orElse(null);
        if (matches != null) {
            boolean isMatched = matches.stream().anyMatch(
                    match ->
                            match.getMate().getMateUuid().equals(mate.getMateUuid())&&
                                    match.getMatched().equals(MatchStatus.MATCHED)
                    &&match.getCompleteStatus().equals(CompleteStatus.INCOMPLETE)
            );

            if (isMatched) {
                throw new MatchingException(ExceptionCode.ALREADY_MATCHED);
            }
        }
        //Owner 와 Mate를 연결시켜줘야함
        Match match = Match.builder()
                .owner(owner)
                .mate(mate)
                .board(findBoardById)
                .build();

        matchRepository.save(match);

    }

    public void matchSuccess(CustomUserDetail userDetail, Long boardId) {
        User findUser = userRepository.findByUuid(userDetail.getUuid()).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );

        Board findBoard = boardRepository.findByBoardId(boardId).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );

        Match findMatch = matchRepository.findById(findBoard.getMatch().getMatchId()).orElseThrow(
                () -> new MatchingException(ExceptionCode.NOT_FOUND_MATCH)
        );
        if (findBoard.getUser().getUuid().equals(findUser.getUuid())) {
            throw new MatchingException(ExceptionCode.ACCEPT_SHOULD_TRY_RECIEVER);
        }
        if(findBoard.getMatched().equals(MatchStatus.MATCHED) && findMatch.getMatched().equals(MatchStatus.MATCHED) ){
            throw new MatchingException(ExceptionCode.ALREADY_ACCEPTED);
        }
        else{
            Match updatedMatch = findMatch.toBuilder()
                    .matched(MatchStatus.MATCHED)
                    .build();

            matchRepository.save(updatedMatch);

            Board board = findBoard.toBuilder()
                    .matched(MatchStatus.MATCHED)
                    .build();

            boardRepository.save(board);
        }

    }

    public void matchFail(CustomUserDetail userDetail, Long boardId) {
        User findUser = userRepository.findByUuid(userDetail.getUuid()).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );

        Board findBoard = boardRepository.findByBoardId(boardId).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );

        Match match = matchRepository.findByBoardAndMate(findBoard, findUser.getMate().get(0)).orElseThrow(
                () -> new MatchingException(ExceptionCode.NOT_FOUND_MATCH)
        );

        Match updatedMatch = match.toBuilder()
                .matched(MatchStatus.UNMATCHED)
                .build();
        matchRepository.save(updatedMatch);


        Board board = findBoard.toBuilder()
                .matched(MatchStatus.UNMATCHED)
                .build();

        boardRepository.save(board);
    }
}
