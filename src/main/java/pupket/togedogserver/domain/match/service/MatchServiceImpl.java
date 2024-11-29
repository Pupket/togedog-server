package pupket.togedogserver.domain.match.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.repository.jpaRepositry.BoardJPARepository;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.match.repository.MatchRepository;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.repository.jpaRepository.OwnerJPARepository;
import pupket.togedogserver.domain.user.repository.jpaRepository.UserJPARepository;
import pupket.togedogserver.domain.user.repository.jpaRepository.MateJPARepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.*;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserJPARepository userRepository;
    private final OwnerJPARepository ownerRepository;
    private final MateJPARepository mateRepository;
    private final BoardJPARepository boardJPARepository;

    @Override
    public void match(CustomUserDetail userDetail, String nickname, Long boardId) {

        //유저 조회
        User findUserByUserDetail = getUser(userRepository.findByEmail(userDetail.getUsername()));

        //닉네임 유효성 검사
        validateIsOwnNickname(nickname, findUserByUserDetail);

        //Owner로 등록되어 있는지 검사
        Owner owner = getOwner(findUserByUserDetail);

        //상대방 유저 검사
        User findUserByNickname = getUser(userRepository.findByNickname(nickname));

        //유저의 메이트 조회
        Mate mate = getMate(findUserByNickname);

        //매칭할 게시판 조회
        Board findBoardById = getBoard(boardJPARepository.findByBoardId(boardId));

        //이미 매칭된 조회라면 예외 던지기
        List<Match> matches = matchRepository.findByOwner(owner);
        if (!matches.isEmpty()) {
            boolean isMatched = matches.stream().anyMatch(
                    match ->
                            match.getMatched().equals(MatchStatus.MATCHED)
                                    || match.getCompleteStatus().equals(CompleteStatus.INCOMPLETE)
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

    private Board getBoard(Optional<Board> boardRepository) {
        //Board
        return boardRepository.orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );
    }

    private Mate getMate(User findUserByNickname) {
        return mateRepository.findByUser(findUserByNickname).orElseThrow(
                () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
        );
    }

    private User getUser(Optional<User> userRepository) {
        //Owner
        return userRepository.orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
    }

    private Owner getOwner(User findUserByUserDetail) {
        return ownerRepository.findByUser(findUserByUserDetail).orElseThrow(
                () -> new OwnerException(ExceptionCode.NOT_FOUND_OWNER)
        );
    }

    private static void validateIsOwnNickname(String nickname, User findUserByUserDetail) {
        if (findUserByUserDetail.getNickname().equals(nickname)) {
            throw new MemberException(ExceptionCode.YOUR_OWN_NICKNAME);
        }
    }

    public void matchSuccess(CustomUserDetail userDetail, Long boardId) {
        //유저 ,게시판 찾기
        User findUser = getUser(userRepository.findByUuid(userDetail.getUuid()));

        //게시판에서 가져올 수 있는 것 -> boardDog
        Board findBoard = getBoard(boardJPARepository.findByBoardId(boardId));

        //매칭된 건인지 확인
        if (findBoard.getMatch() == null) {
            throw new MatchingException(ExceptionCode.NOT_FOUND_MATCH);
        }

        Match findMatch = getMatch(matchRepository.findById(findBoard.getMatch().getMatchId()));

        if (findBoard.getUser().getUuid().equals(findUser.getUuid())) {
            throw new MatchingException(ExceptionCode.ACCEPT_SHOULD_TRY_RECIEVER);
        }

        if (findBoard.getMatched().equals(MatchStatus.MATCHED)) {
            throw new MatchingException(ExceptionCode.ALREADY_ACCEPTED);
        }
        Match updatedMatch = findMatch.toBuilder()
                .matched(MatchStatus.MATCHED)
                .build();

        matchRepository.save(updatedMatch);

        Board board = findBoard.toBuilder()
                .matched(MatchStatus.MATCHED)
                .build();

        boardJPARepository.save(board);

    }

    private Match getMatch(Optional<Match> matchRepository) {
        return matchRepository.orElseThrow(
                () -> new MatchingException(ExceptionCode.NOT_FOUND_MATCH)
        );
    }

    public void matchFail(CustomUserDetail userDetail, Long boardId) {
        User findUser = getUser(userRepository.findByUuid(userDetail.getUuid()));

        Board findBoard = getBoard(boardJPARepository.findByBoardId(boardId));

        Mate findMate = findUser.getMate();
        if (findMate == null) {
            throw new MatchingException(ExceptionCode.NOT_FOUND_MATCH);
        }

        Match match = getMatch(matchRepository.findByBoardAndMate(findBoard, findMate));

        Match updatedMatch = match.toBuilder()
                .matched(MatchStatus.UNMATCHED)
                .build();

        matchRepository.save(updatedMatch);

        Board board = findBoard.toBuilder()
                .matched(MatchStatus.UNMATCHED)
                .build();

        boardJPARepository.save(board);
    }

    public void completeWalking(Long boardId, CustomUserDetail userDetail) {
        User findUser = getUser(userRepository.findByUuid(userDetail.getUuid()));

        Mate findMate = mateRepository.findByUser(findUser).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );

        Board findBoard = getBoard(boardJPARepository.findById(boardId));

        Match findMatch = getMatch(matchRepository.findByBoardAndMate(findBoard, findMate));

        Match completedMatch = updateMatchToComplete(findMatch);

        Mate updatedMate = findMate.toBuilder()
                .matchCount(findMate.getMatchCount() + 1)
                .build();

        Owner owner = findUser.getOwner();

        Owner updatedOwner = owner.toBuilder()
                .matchCount(owner.getMatchCount() + 1)
                .build();

        mateRepository.save(updatedMate);
        ownerRepository.save(updatedOwner);
        matchRepository.save(completedMatch);
    }

    private static Match updateMatchToComplete(Match findMatch) {
        if (findMatch.getCompleteStatus().equals(CompleteStatus.COMPLETE)) {
            throw new MatchingException(ExceptionCode.ALREADY_COMPLETED);
        }

        return findMatch.toBuilder()
                .completeStatus(CompleteStatus.COMPLETE)
                .build();
    }
}
