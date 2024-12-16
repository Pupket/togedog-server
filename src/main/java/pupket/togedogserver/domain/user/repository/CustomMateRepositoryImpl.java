package pupket.togedogserver.domain.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.dto.response.BoardDogResponse;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.dto.response.MateActiveResponse;
import pupket.togedogserver.domain.user.dto.response.PreferredDetailsResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MateTag;
import pupket.togedogserver.domain.user.repository.jpaRepository.MateJPARepository;
import pupket.togedogserver.domain.user.service.port.CustomMateRepository;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MateException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
@Slf4j
public class CustomMateRepositoryImpl implements CustomMateRepository {

    private final EntityManager em;
    private final MateJPARepository mateRepository;
    private final UserRepository userRepository;

    @Override
    public Page<FindMateResponse> MateList(Pageable pageable, Long uuid) {
        User user = userRepository.findByUuid(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
        List<Mate> mateList = null;
        if (user.getMate() != null) {
            mateList = getMates(pageable,user.getMate().getMateUuid()); //query로 mate결과값 가져오기
        }else{
            mateList = getMates(pageable,null);
        }

        Long count = getCount(); //Count쿼리로 결과수 가져오기

        // 엔티티를 DTO로 변환
        List<FindMateResponse> mateResponse = mateList.stream()
                .map(mate -> {
                    PreferredDetailsResponse preferred = getPreferredDetailsResponse(mate);

                    Mate findMate = mateRepository.findById(mate.getMateUuid()).orElseThrow(
                            () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
                    );

                    preferred.setRegion(String.valueOf(EnumMapper.enumToKorean(findMate.getPreferredRegion())));

                    // birthday를 4자리로 맞추기 (3자리면 앞에 0 추가)
                    String birthday = String.valueOf(mate.getUser().getBirthday());
                    if (birthday.length() == 3) {
                        birthday = "0" + birthday; // 앞에 0을 붙여 4자리로 만듦
                    }

                    return FindMateResponse.builder()
                            .uuid(mate.getUser().getUuid())
                            .mateId(mate.getUser().getMate().getMateUuid())
                            .nickname(mate.getUser().getNickname())
                            .profileImage(mate.getUser().getProfileImage())
                            .gender(EnumMapper.enumToKorean(mate.getUser().getUserGender())) // gender 변환
                            .age(LocalDateTime.now().getYear() - mate.getUser().getBirthyear())
                            .birth(mate.getUser().getBirthyear() + "." + birthday.substring(0, 2) + "." + birthday.substring(2, 4))
                            .accommodatableDogsCount(mate.getAccommodatableDogsCount())
                            .career(mate.getCareer())
                            .preferred(preferred)
                            .build();
                }).toList();

        return new PageImpl<>(mateResponse, pageable, count);
    }

    @Override
    public Page<BoardFindResponse> findMyScheduleList(Long mateId, Pageable pageable) {
        // 1. Board 조회 (조건: Match의 mateId와 매칭된 Board)
        String boardQuery = "SELECT DISTINCT b FROM Board b " +
                "JOIN b.match m " +
                "WHERE b.deleted = false AND m.mate.mateUuid = :mateId " +
                "AND m.matched = :matchedStatus";

        List<Board> boards = em.createQuery(boardQuery, Board.class)
                .setParameter("mateId", mateId)
                .setParameter("matchedStatus", MatchStatus.MATCHED) // MATCHED 상태만 가져옴
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        if (boards.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. BoardDog 조회
        String boardDogQuery = "SELECT bd FROM BoardDog bd " +
                "JOIN FETCH bd.dog d " +
                "WHERE bd.board IN :boards";

        List<BoardDog> boardDogs = em.createQuery(boardDogQuery, BoardDog.class)
                .setParameter("boards", boards)
                .getResultList();

        // 3. 데이터 매핑
        Map<Long, List<BoardDog>> boardDogMap = boardDogs.stream()
                .collect(Collectors.groupingBy(bd -> bd.getBoard().getBoardId()));

        List<BoardFindResponse> boardResponses = boards.stream()
                .map(board -> {
                    List<BoardDog> boardDogList = boardDogMap.getOrDefault(board.getBoardId(), List.of());
                    List<Dog> dogs = boardDogList.stream()
                            .map(BoardDog::getDog)
                            .toList();

                    return BoardFindResponse.builder()
                            .boardId(board.getBoardId())
                            .userId(board.getUser().getUuid())
                            .title(board.getTitle())
                            .pickUpDay(board.getPickUpDay())
                            .fee(EnumMapper.enumToKorean(board.getFee()))
                            .feeType(EnumMapper.enumToKorean(board.getFeeType()))
                            .startTime(String.valueOf(board.getStartTime()))
                            .endTime(String.valueOf(board.getEndTime()))
                            .pickupLocation1(board.getPickupLocation1())
                            .walkingPlaceTag(board.getWalkingPlaceTag().stream()
                                    .map(WalkingPlaceTag::getPlaceName)
                                    .toList())
                            .dogs(dogs.stream()
                                    .map(dog -> BoardDogResponse.builder()
                                            .name(dog.getName())
                                            .age(dog.getAge())
                                            .breed(EnumMapper.enumToKorean(dog.getBreed()))
                                            .dogType(EnumMapper.enumToKorean(dog.getDogType()))
                                            .dogGender(dog.getDogGender() ? "수컷" : "암컷")
                                            .dogProfileImage(dog.getDogImage())
                                            .build())
                                    .toList())
                            .completeStatus(CompleteStatus.INCOMPLETE.getStatus()) // INCOMPLETE 상태 처리
                            .build();
                })
                .toList();

        // 4. 전체 Board 수 조회
        String countQuery = "SELECT COUNT(DISTINCT b) FROM Board b " +
                "JOIN b.match m " +
                "WHERE b.deleted = false AND m.mate.mateUuid = :mateId " +
                "AND m.matched = :matchedStatus";

        Long count = em.createQuery(countQuery, Long.class)
                .setParameter("mateId", mateId)
                .setParameter("matchedStatus", MatchStatus.MATCHED) // MATCHED 상태만 카운트
                .getSingleResult();

        return new PageImpl<>(boardResponses, pageable, count);
    }

    @Override
    public MateActiveResponse findMateActions(Long mateUuid, User findUser) {
        String query = "select b from " +
                "Board b join matching m " +
                "on m.completeStatus = 'COMPLETE' " +
                "and b.boardId = m.board.boardId " +
                "where m.mate.mateUuid= :mateUuid " +
                " and b.createdAt between :startOfMonth and :endOfMonth";

        TypedQuery<Board> result = em.createQuery(query, Board.class);
        // 이번 달의 시작과 끝 날짜 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

        result.setParameter("mateUuid", mateUuid);
        result.setParameter("startOfMonth", startOfMonth);
        result.setParameter("endOfMonth", endOfMonth);

        List<Board> results = result.getResultList();

        MateActiveResponse mateActiveResponse = MateActiveResponse.builder()
                .mateName(findUser.getName())
                .walkCount(findUser.getMate().getMatchCount())
                .build();

        AtomicLong totalHours = new AtomicLong(0L);
        AtomicLong totalMinutes = new AtomicLong(0L);

        log.info("{}", results.size());
        results.forEach(
                i -> {
                    LocalDateTime startTime = i.getStartTime();
                    LocalDateTime endTime = i.getEndTime();

                    // 시작 시간과 종료 시간 사이의 차이를 계산
                    Duration duration = Duration.between(startTime, endTime);
                    long durationMinutes = duration.toHours();
                    log.info("hours={}", durationMinutes);
                    long minutes = duration.toMinutes() % 60;
                    log.info("minute= {}", minutes);

                    totalHours.addAndGet(durationMinutes);
                    totalMinutes.addAndGet(minutes);
                }
        );

        String resultHour = totalHours.get() + "시간";
        String resultMinute = totalMinutes.get() >= 30 ? "30분" : "";

        // mateActiveResponse에 시간과 분을 설정
        mateActiveResponse.setWalkTime(resultHour + " " + resultMinute);


        return mateActiveResponse;
    }

    private PreferredDetailsResponse getPreferredDetailsResponse(Mate mate) {
        return PreferredDetailsResponse.builder()
                .week(mate.getPreferredWeeks().stream()
                        .map(week -> EnumMapper.enumToKorean(week.getPreferredWeek())) // preferredWeek 변환
                        .collect(Collectors.toSet()))
                .time(mate.getPreferredTimes().stream()
                        .map(time -> EnumMapper.enumToKorean(time.getPreferredTime())) // preferredTime 변환
                        .collect(Collectors.toSet()))
                .hashTag(mate.getMateTags().stream()
                        .map(MateTag::getTagName)
                        .collect(Collectors.toSet()))
                .breed(mate.getPreferredBreeds().stream()
                        .map(breed -> EnumMapper.enumToKorean(breed.getPreferredDogType())) // preferredBreed 변환
                        .collect(Collectors.toSet()))
                .build();
    }

    private Long getCount() {
        // JPQL로 전체 개수 쿼리 작성
        String countJpql = "SELECT COUNT(b) FROM Mate b WHERE b.deleted = false";
        return em.createQuery(countJpql, Long.class).getSingleResult();
    }

    private List<Mate> getMates(Pageable pageable, Long uuid) {
        String query= null;
        TypedQuery<Mate> result=null;
        if (uuid == null) {
            query = "SELECT b FROM Mate b WHERE b.deleted = false order by rand()";

            result = em.createQuery(query, Mate.class);
            result.setFirstResult((int) pageable.getOffset());
            result.setMaxResults(pageable.getPageSize());
        }else{
            query = "SELECT b FROM Mate b WHERE b.mateUuid!=:uuid AND b.deleted = false order by rand()";
            result = em.createQuery(query, Mate.class);
            result.setFirstResult((int) pageable.getOffset());
            result.setParameter("uuid", uuid);
            result.setMaxResults(pageable.getPageSize());
        }

        return result.getResultList();
    }
}

