package pupket.togedogserver.domain.match.service;

import com.amazonaws.auth.SdkClock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;
import pupket.togedogserver.domain.board.service.port.BoardRepository;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.match.controller.port.MatchService;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.match.service.port.MatchRepository;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDtoForMatching;
import pupket.togedogserver.domain.notification.service.NotificationServiceImpl;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.service.port.MateRepository;
import pupket.togedogserver.domain.user.service.port.OwnerRepository;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.*;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final MateRepository mateRepository;
    private final BoardRepository boardRepository;
    private final NotificationServiceImpl notificationServiceImpl;

    @Override
    @Transactional
    public void match(CustomUserDetail userDetail, String nickname, Long boardId) {
        log.info("매칭 시작: 사용자={}, 닉네임={}, 게시판ID={}", userDetail.getUsername(), nickname, boardId);

        //유저 조회
        User findUserByUserDetail = getUser(userRepository.findByEmail(userDetail.getUsername()));

        //닉네임 유효성 검사
        validateIsOwnNickname(nickname, findUserByUserDetail);

        //Owner로 등록되어 있는지 검사
        Owner owner = getOwner(findUserByUserDetail);

        //상대방 유저 검사
        User findUserByNickname = getUser(userRepository.findByNickname(nickname));

        //유저의 메이트 조회
        Mate findMate = getMate(findUserByNickname);

        //매칭할 게시판 조회
        Board findBoard = getBoard(boardRepository.findByBoardId(boardId));

        //이미 매칭된 조회라면 예외 던지기
        List<Match> conflictMatches = matchRepository.findConflictMatches(findMate.getMateUuid(), findBoard.getStartTime(), findBoard.getEndTime(), findBoard.getPickUpDay());

        if (!conflictMatches.isEmpty()) {
            conflictMatches.forEach(match -> log.info("겹치는 일정: startTime={}, endTime={}, pickUpDay={}",
                    match.getBoard().getStartTime(),
                    match.getBoard().getEndTime(),
                    match.getBoard().getPickUpDay()));
            throw new MatchingException(ExceptionCode.SCHDULE_CONFICT);
        }

        //Owner 와 Mate를 연결시켜줘야함
        Match match = Match.builder()
                .owner(owner)
                .mate(findMate)
                .board(findBoard)
                .build();

        matchRepository.save(match);
        log.info("매칭 저장 완료: 매칭ID={}", match.getMatchId());

        sendMatchingNotification(findBoard, match);

    }

    private void sendMatchingNotification(Board findBoard, Match match) {
        String message ="";
        List<BoardDog> boardDog = match.getBoard().getBoardDog();
        for(BoardDog boarddogEntity : boardDog){
            message += boarddogEntity.getDog().getName()+ " ";
        }
        //알림생성
        NotificationRequestDtoForMatching notificationRequestDtoForMatching = NotificationRequestDtoForMatching.builder()
                .boardId(findBoard.getBoardId())
                .title("산책 매칭 요청")
                .userId(match.getMate().getUser().getUuid())
                .message(message+"의 보호자가 산책 매칭을 요청하였습니다.")
                .timestamp(Timestamp.from(Instant.now()))
                .build();

            //알림 전송
            notificationServiceImpl.sendNotificationAboutMatching(notificationRequestDtoForMatching, match.getMate().getUser());

    }

    private static void validateMatcing(List<Match> matches) {
        if (!matches.isEmpty()) {
            boolean isMatched = matches.stream().anyMatch(
                    match ->
                            match.getMatched().equals(MatchStatus.MATCHED)
                                    || match.getCompleteStatus().equals(CompleteStatus.COMPLETE)
            );

            if (isMatched) {
                log.warn("이미 매칭된 상태입니다.");
                throw new MatchingException(ExceptionCode.ALREADY_MATCHED);
            }
        }
    }

    private Board getBoard(Optional<Board> boardRepository) {
        //Board
        return boardRepository.orElseThrow(
                () -> {
                    log.error("게시판을 찾을 수 없습니다.");
                    return new BoardException(ExceptionCode.NOT_FOUND_BOARD);
                }
        );
    }

    private Mate getMate(User findUserByNickname) {
        return mateRepository.findByUser(findUserByNickname).orElseThrow(
                () -> {
                    log.error("메이트를 찾을 수 없습니다.");
                    return new MateException(ExceptionCode.NOT_FOUND_MATE);
                }
        );
    }

    private User getUser(Optional<User> userRepository) {
        //Owner
        return userRepository.orElseThrow(
                () -> {
                    log.error("사용자를 찾을 수 없습니다.");
                    return new MemberException(ExceptionCode.NOT_FOUND_MEMBER);
                }
        );
    }

    private Owner getOwner(User findUserByUserDetail) {
        return ownerRepository.findByUser(findUserByUserDetail).orElseThrow(
                () -> {
                    log.error("소유자를 찾을 수 없습니다.");
                    return new OwnerException(ExceptionCode.NOT_FOUND_OWNER);
                }
        );
    }

    private static void validateIsOwnNickname(String nickname, User findUserByUserDetail) {
        if (findUserByUserDetail.getNickname().equals(nickname)) {
            log.warn("자신의 닉네임을 사용할 수 없습니다.");
            throw new MemberException(ExceptionCode.YOUR_OWN_NICKNAME);
        }
    }

    @Override
    public void matchSuccess(CustomUserDetail userDetail, Long boardId) {
        log.info("매칭 성공 처리 시작: 사용자={}, 게시판ID={}", userDetail.getUsername(), boardId);

        // 메이트 유저 조회
        User findUser = getUser(userRepository.findByUuid(userDetail.getUuid()));
        //해당 게시판 조회
        Board findBoard = getBoard(boardRepository.findByBoardId(boardId));

        // 매칭된 건인지 확인
        if (findBoard.getMatch() == null) {
            log.warn("매칭을 찾을 수 없습니다.");
            throw new MatchingException(ExceptionCode.NOT_FOUND_MATCH);
        }

        //수락자가 자신이 아닌지, 이미 매칭된 게시글인지 체크
        validateMatchStatus(findBoard, findUser);

        //Match 조회
        Match findMatch = getMatch(matchRepository.findById(findBoard.getMatch().getMatchId()));

        //매칭 성공으로 업데이트
        updateMatchAndBoardMatchStatusToMacthed(findMatch, findBoard);
        log.info("매칭 성공 처리 완료: 매칭ID={}", findMatch.getMatchId());

        //알림생성
        NotificationRequestDtoForMatching notificationRequestDtoForMatching = NotificationRequestDtoForMatching.builder()
                .boardId(findBoard.getBoardId())
                .title("산책 매칭 수락")
                .userId(findMatch.getOwner().getUser().getUuid())
                .message(findMatch.getMate().getUser().getNickname() + "님이 산책 매칭을 수락했어요.")
                .build();

        try {
            //알림 전송
            notificationServiceImpl.sendNotificationAboutMatching(notificationRequestDtoForMatching, findMatch.getOwner().getUser());
        } catch (Exception e) {
            log.error("Matching Fail notification Message Error 발생");
            log.error(e.getMessage());
        }
    }

    private static void validateMatchStatus(Board findBoard, User findUser) {
        // null 체크는 이미 수행되었으므로 제거
        if (findBoard.getUser().getUuid().equals(findUser.getUuid())) {
            log.warn("수락은 수신자가 시도해야 합니다.");
            throw new MatchingException(ExceptionCode.ACCEPT_SHOULD_TRY_RECIEVER);
        }

        if (findBoard.getMatched().equals(MatchStatus.MATCHED)) {
            log.warn("이미 수락된 상태입니다.");
            throw new MatchingException(ExceptionCode.ALREADY_ACCEPTED);
        }
    }

    private void updateMatchAndBoardMatchStatusToMacthed(Match findMatch, Board findBoard) {
        Match updatedMatch = findMatch.toBuilder()
                .matched(MatchStatus.MATCHED)
                .build();

        updatedMatch = matchRepository.save(updatedMatch);

        Board board = findBoard.toBuilder()
                .matched(MatchStatus.MATCHED)
                .build();

        boardRepository.save(board);
    }

    private Match getMatch(Optional<Match> matchRepository) {
        return matchRepository.orElseThrow(
                () -> {
                    log.error("매칭을 찾을 수 없습니다.");
                    return new MatchingException(ExceptionCode.NOT_FOUND_MATCH);
                }
        );
    }

    @Override
    public void matchFail(CustomUserDetail userDetail, Long boardId) {
        log.info("매칭 실패 처리 시작: 사용자={}, 게시판ID={}", userDetail.getUsername(), boardId);

        //유저 조회
        User findUser = getUser(userRepository.findByUuid(userDetail.getUuid()));

        //해당 게시판 조회
        Board findBoard = getBoard(boardRepository.findByBoardId(boardId));

        //메이트 조회
        Mate findMate = findUser.getMate();
        if (findMate == null) {
            log.warn("메이트를 찾을 수 없습니다.");
            throw new MatchingException(ExceptionCode.NOT_FOUND_MATCH);
        }

        //매칭 조회
        Match findMatch = getMatch(matchRepository.findByBoardAndMate(findBoard, findMate));


        // 해당 매칭 거절 처리
        updateMatchAndBoardToUnmatched(findMatch, findBoard);
        log.info("매칭 실패 처리 완료: 매칭ID={}", findMatch.getMatchId());

        //알림 생성
        NotificationRequestDtoForMatching notificationRequestDtoForMatching = NotificationRequestDtoForMatching.builder()
                .boardId(findBoard.getBoardId())
                .title("산책 매칭 거절")
                .userId(findMatch.getOwner().getUser().getUuid())
                .message(findMatch.getMate().getUser().getNickname() + "님이 산책 매칭을 거절했어요.")
                .build();
        try {
            //알림 전송
            notificationServiceImpl.sendNotificationAboutMatching(notificationRequestDtoForMatching, findMatch.getOwner().getUser());
        } catch (Exception e) {
            log.error("Matching Fail notification Message Error 발생");
            log.error(e.getMessage());
        }
    }

    private void updateMatchAndBoardToUnmatched(Match match, Board findBoard) {
        Match updatedMatch = match.toBuilder()
                .matched(MatchStatus.UNMATCHED)
                .build();

        matchRepository.save(updatedMatch);

        Board board = findBoard.toBuilder()
                .matched(MatchStatus.UNMATCHED)
                .build();

        boardRepository.save(board);
    }

    @Override
    public void completeWalking(Long boardId, CustomUserDetail userDetail) {
        log.info("산책 완료 처리 시작: 사용자={}, 게시판ID={}", userDetail.getUsername(), boardId);

        //유저 조회
        User findUser = getUser(userRepository.findByUuid(userDetail.getUuid()));

        //메이트 조회
        Mate findMate = getMate(findUser);

        //게시판 조회
        Board findBoard = getBoard(boardRepository.findById(boardId));

        //매칭조회
        Match findMatch = getMatch(matchRepository.findByBoardAndMate(findBoard, findMate));

        //매칭 완료로 업데이트
        Match completedMatch = updateMatchToComplete(findMatch);

        //각 유저 매치 카운트 증가
        findMate.setMatchCount(findMate.getMatchCount() + 1);
        Owner owner = findUser.getOwner();

        owner.setMatchCount(owner.getMatchCount() + 1);

        mateRepository.save(findMate);
        ownerRepository.save(owner);
        matchRepository.save(completedMatch);

        log.info("산책 완료 처리 완료: 매칭ID={}", completedMatch.getMatchId());

        //알림 생성
        NotificationRequestDtoForMatching notificationRequestDtoForMatching = NotificationRequestDtoForMatching.builder()
                .boardId(findBoard.getBoardId())
                .title("산책 진행 현")
                .userId(findMatch.getOwner().getUser().getUuid())
                .message(findMatch.getMate().getUser().getNickname() + "님이 산책을 완료하셨습니다!")
                .build();
        try {
            //알림 전송
            notificationServiceImpl.sendNotificationAboutMatching(notificationRequestDtoForMatching, findMatch.getOwner().getUser());
        } catch (Exception e) {
            log.error("Matching Fail notification Message Error 발생");
            log.error(e.getMessage());
        }
    }

    private static Match updateMatchToComplete(Match findMatch) {
        if (findMatch.getCompleteStatus().equals(CompleteStatus.COMPLETE)) {
            log.warn("이미 완료된 상태입니다.");
            throw new MatchingException(ExceptionCode.ALREADY_COMPLETED);
        }

        return findMatch.toBuilder()
                .completeStatus(CompleteStatus.COMPLETE)
                .build();
    }
}
