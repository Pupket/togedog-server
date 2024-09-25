package pupket.togedogserver.domain.user.repository.mateRepo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.dto.response.BoardDogResponse;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.dto.response.PreferredDetailsResponse;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MateTag;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MateException;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class CustomMateRepositoryImpl implements CustomMateRepository {

    private final EntityManager em;
    private final MateRepository mateRepository;

    @Override
    public Page<FindMateResponse> MateList(Pageable pageable) {
        String query = "SELECT b FROM Mate b WHERE b.deleted = false order by rand()";
        TypedQuery<Mate> result = em.createQuery(query, Mate.class);

        result.setFirstResult((int) pageable.getOffset());
        result.setMaxResults(pageable.getPageSize());
        List<Mate> mateList = result.getResultList();

        // JPQL로 전체 개수 쿼리 작성
        String countJpql = "SELECT COUNT(b) FROM Mate b WHERE b.deleted = false";
        Long count = em.createQuery(countJpql, Long.class).getSingleResult();

        // 엔티티를 DTO로 변환
        List<FindMateResponse> MateResponse = mateList.stream()
                .map(mate -> {
                    PreferredDetailsResponse preferred = PreferredDetailsResponse.builder()
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
                            .mateId(mate.getUser().getMate().get(0).getMateUuid())
                            .nickname(mate.getUser().getNickname())
                            .profileImage(mate.getUser().getProfileImage())
                            .gender(EnumMapper.enumToKorean(mate.getUser().getUserGender())) // gender 변환
                            .age(LocalDateTime.now().getYear() - mate.getUser().getBirthyear())
                            .birth(mate.getUser().getBirthyear() + "." + birthday.substring(0, 2) + "." + birthday.substring(2, 4))
                            .accommodatableDogsCount(mate.getAccommodatableDogsCount())
                            .career(mate.getCareer())
                            .preferred(preferred)
                            .build();
                }).collect(Collectors.toList());

        return new PageImpl<>(MateResponse, pageable, count);
    }

    public Page<BoardFindResponse> findMyScheduleList(Long mateId, Pageable pageable) {
        // 여러 마리의 개를 처리할 수 있도록 Board와 Dog 테이블을 JOIN
        String query = "SELECT b, d FROM Board b " +
                "JOIN b.boardDog bd " +  // BoardDog 테이블도 추가로 JOIN
                "JOIN bd.dog d " +  // BoardDog과 Dog을 JOIN
                "WHERE b.deleted = false AND d.deleted = false " +
                "AND b.match.mate.mateUuid = :mateId";

        TypedQuery<Object[]> result = em.createQuery(query, Object[].class);
        result.setParameter("mateId", mateId); // mateId 파라미터 설정
        result.setFirstResult((int) pageable.getOffset());
        result.setMaxResults(pageable.getPageSize());

        List<Object[]> results = result.getResultList();
        Map<Long, BoardFindResponse> boardResponseMap = new HashMap<>();

        // 각 Board에 여러 마리의 Dog 정보를 추가
        results.forEach(row -> {
            Board board = (Board) row[0];
            Dog dog = (Dog) row[1];

            // 이미 Board가 추가되었는지 확인하고 없다면 새로 추가
            boardResponseMap.computeIfAbsent(board.getBoardId(), boardId -> {
                String fee = EnumMapper.enumToKorean(board.getFee());
                String feeType = EnumMapper.enumToKorean(board.getFeeType());
                String startTime = String.valueOf(board.getStartTime());
                String endTime = String.valueOf(board.getEndTime());
                List<String> walkingPlaceTags = board.getWalkingPlaceTag().stream()
                        .map(WalkingPlaceTag::getPlaceName)
                        .collect(Collectors.toList());

                return BoardFindResponse.builder()
                        .boardId(board.getBoardId())
                        .userId(board.getUser().getUuid())
                        .title(board.getTitle())
                        .pickUpDay(board.getPickUpDay())
                        .fee(fee)
                        .feeType(feeType)
                        .startTime(startTime)
                        .endTime(endTime)
                        .pickupLocation1(board.getPickupLocation1())
                        .walkingPlaceTag(walkingPlaceTags)
                        .dogs(new ArrayList<>())  // Dog 정보를 담을 리스트 초기화
                        .build();
            });

            // Dog 정보를 DogResponse로 변환하여 추가
            BoardFindResponse response = boardResponseMap.get(board.getBoardId());
            response.getDogs().add(BoardDogResponse.builder()
                    .name(dog.getName())
                    .age(dog.getAge())
                    .breed(EnumMapper.enumToKorean(dog.getBreed()))
                    .dogType(EnumMapper.enumToKorean(dog.getDogType()))
                    .dogGender(dog.getDogGender() ? "수컷" : "암컷")
                    .dogProfileImage(dog.getDogImage())
                    .build());
        });

        List<BoardFindResponse> boardResponses = new ArrayList<>(boardResponseMap.values());

        // 카운트 쿼리
        String countQuery = "SELECT COUNT(b) FROM Board b " +
                "JOIN b.boardDog bd " +
                "JOIN bd.dog d " +
                "WHERE b.deleted = false AND d.deleted = false AND b.match.mate.mateUuid = :mateId";

        Long count = em.createQuery(countQuery, Long.class)
                .setParameter("mateId", mateId)
                .getSingleResult();

        return new PageImpl<>(boardResponses, pageable, count);
    }

}

