package pupket.togedogserver.domain.user.repository.mateRepo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
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
import java.util.List;
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
                .map(board -> {
                    PreferredDetailsResponse preferred = PreferredDetailsResponse.builder()
                            .week(board.getPreferredWeeks().stream()
                                    .map(week -> EnumMapper.enumToKorean(week.getPreferredWeek())) // preferredWeek 변환
                                    .collect(Collectors.toSet()))
                            .time(board.getPreferredTimes().stream()
                                    .map(time -> EnumMapper.enumToKorean(time.getPreferredTime())) // preferredTime 변환
                                    .collect(Collectors.toSet()))
                            .hashTag(board.getMateTags().stream()
                                    .map(MateTag::getTagName)
                                    .collect(Collectors.toSet()))
                            .breed(board.getPreferredBreeds().stream()
                                    .map(breed -> EnumMapper.enumToKorean(breed.getPreferredDogType())) // preferredBreed 변환
                                    .collect(Collectors.toSet()))
                            .build();

                    Mate findMate = mateRepository.findById(board.getMateUuid()).orElseThrow(
                            () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
                    );

                    preferred.setRegion(String.valueOf(EnumMapper.enumToKorean(findMate.getPreferredRegion())));


                    return FindMateResponse.builder()
                            .uuid(board.getUser().getUuid())
                            .mateId(board.getUser().getMate().get(0).getMateUuid())
                            .nickname(board.getUser().getNickname())
                            .profileImage(board.getUser().getProfileImage())
                            .gender(EnumMapper.enumToKorean(board.getUser().getUserGender())) // gender 변환
                            .age(LocalDateTime.now().getYear() - board.getUser().getBirthyear())
                            .birth(board.getUser().getBirthyear() + "." + String.valueOf(board.getUser().getBirthday()).substring(0, 2) + "." + String.valueOf(board.getUser().getBirthday()).substring(2, 4))
                            .accommodatableDogsCount(board.getAccommodatableDogsCount())
                            .career(board.getCareer())
                            .preferred(preferred)
                            .build();
                }).collect(Collectors.toList());

        return new PageImpl<>(MateResponse, pageable, count);
    }

    public Page<BoardFindResponse> findMyScheduleList(Long mateId, Pageable pageable) {
        String query = "SELECT b, d FROM Board b " +
                "JOIN Dog d ON b.dog = d " +
                "WHERE b.deleted = false AND d.deleted = false " +
                "AND b.match.mate.mateUuid = :mateId";
        TypedQuery<Object[]> result = em.createQuery(query, Object[].class);
        result.setParameter("mateId", mateId); // uuid 파라미터에 값 설정
        result.setFirstResult((int) pageable.getOffset());
        result.setMaxResults(pageable.getPageSize());

        List<Object[]> results = result.getResultList();
        List<BoardFindResponse> boardResponses = results.stream()
                .map(row -> {
                    Board board = (Board) row[0];
                    Dog dog = (Dog) row[1];

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
                                    .collect(Collectors.toList()))
                            .name(dog.getName())
                            .age(dog.getAge())
                            .breed(EnumMapper.enumToKorean(dog.getBreed()))
                            .dogType(EnumMapper.enumToKorean(dog.getDogType()))
                            .dogGender(dog.getDogGender()?"수컷":"암컷")
                            .dogProfileImage(dog.getDogImage())
                            .build();
                })
                .collect(Collectors.toList());

        String countQuery = "SELECT COUNT(b) FROM Board b JOIN Dog d ON b.dog = d WHERE b.deleted = false AND d.deleted = false AND b.user.uuid = :uuid";
        Long count = em.createQuery(countQuery, Long.class)
                .setParameter("uuid", mateId)
                .getSingleResult();

        return new PageImpl<>(boardResponses, pageable, count);
    }
}

