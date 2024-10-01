package pupket.togedogserver.domain.match.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.repository.BoardDogRepository;
import pupket.togedogserver.domain.board.repository.BoardRepository;
import pupket.togedogserver.domain.dog.repository.DogRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final MateRepository mateRepository;
    private final BoardRepository boardRepository;

    @Override
    public void match(CustomUserDetail userDetail, String nickname, Long boardId) {

        User findUserByUserDetail = getUser(userRepository.findByEmail(userDetail.getUsername()));

        validateNickname(nickname, findUserByUserDetail);

        Owner owner = getOwner(findUserByUserDetail);

        User findUserByNickname = getUser(userRepository.findByNickname(nickname));

        Mate mate = getMate(findUserByNickname);

        Board findBoardById = getBoard(boardRepository.findByBoardId(boardId));

        List<Match> matches = matchRepository.findByOwner(owner).orElse(null);
        if (matches != null) {
            boolean isMatched = matches.stream().anyMatch(
                    match ->
                            match.getMate().getMateUuid().equals(mate.getMateUuid()) &&
                                    match.getMatched().equals(MatchStatus.MATCHED)
                                    && match.getCompleteStatus().equals(CompleteStatus.INCOMPLETE)
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
        Board findBoardById = boardRepository.orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );
        return findBoardById;
    }

    private Mate getMate(User findUserByNickname) {
        Mate mate = mateRepository.findByUser(findUserByNickname).orElseThrow(
                () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
        );
        return mate;
    }

    private User getUser(Optional<User> userRepository) {
        //Owner
        User findUserByUserDetail = userRepository.orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
        return findUserByUserDetail;
    }

    private Owner getOwner(User findUserByUserDetail) {
        Owner owner = ownerRepository.findByUser(findUserByUserDetail).orElseThrow(
                () -> new OwnerException(ExceptionCode.NOT_FOUND_OWNER)
        );
        return owner;
    }

    private static void validateNickname(String nickname, User findUserByUserDetail) {
        if (findUserByUserDetail.getNickname().equals(nickname)) {
            throw new MemberException(ExceptionCode.YOUR_OWN_NICKNAME);
        }
    }

    public void matchSuccess(CustomUserDetail userDetail, Long boardId) {
        //유저 ,게시판 찾기
        User findUser = getUser(userRepository.findByUuid(userDetail.getUuid()));

        //게시판에서 가져올 수 있는 것 -> boardDog
        Board findBoard = getBoard(boardRepository.findByBoardId(boardId));

        Match findMatch = getMatch(matchRepository.findById(findBoard.getMatch().getMatchId()));
        if (findBoard.getUser().getUuid().equals(findUser.getUuid())) {
            throw new MatchingException(ExceptionCode.ACCEPT_SHOULD_TRY_RECIEVER);
        }
        if (findBoard.getMatched().equals(MatchStatus.MATCHED) && findMatch.getMatched().equals(MatchStatus.MATCHED)) {
            throw new MatchingException(ExceptionCode.ALREADY_ACCEPTED);
        } else {
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

    private Match getMatch(Optional<Match> matchRepository) {
        Match findMatch = matchRepository.orElseThrow(
                () -> new MatchingException(ExceptionCode.NOT_FOUND_MATCH)
        );
        return findMatch;
    }

    public void matchFail(CustomUserDetail userDetail, Long boardId) {
        User findUser = getUser(userRepository.findByUuid(userDetail.getUuid()));

        Board findBoard = getBoard(boardRepository.findByBoardId(boardId));

        Match match = getMatch(matchRepository.findByBoardAndMate(findBoard, findUser.getMate()));

        Match updatedMatch = match.toBuilder()
                .matched(MatchStatus.UNMATCHED)
                .build();

        matchRepository.save(updatedMatch);

        Board board = findBoard.toBuilder()
                .matched(MatchStatus.UNMATCHED)
                .build();

        boardRepository.save(board);
    }

    public void completeWalking(Long boardId, CustomUserDetail userDetail) {
        User findUser = getUser(userRepository.findByUuid(userDetail.getUuid()));

        Mate findMate = mateRepository.findByUser(findUser).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );

        Board findBoard = getBoard(boardRepository.findById(boardId));

        Match findMatch = getMatch(matchRepository.findByBoardAndMate(findBoard, findMate));

        Match completedMatch = updateMatchToComplete(findMatch);

        Mate updatedMate = findMate.toBuilder()
                .matchCount(findMate.getMatchCount() + 1)
                .build();
        Owner owner = findBoard.getUser().getOwner();
        Owner updatedOwner = owner.toBuilder()
                .matchCount(owner.getMatchCount() + 1)
                .build();

        mateRepository.save(updatedMate);
        ownerRepository.save(updatedOwner);
        matchRepository.save(completedMatch);
    }

    private static Match updateMatchToComplete(Match findMatch) {
        return findMatch.toBuilder()
                .completeStatus(CompleteStatus.COMPLETE)
                .build();
    }
}
