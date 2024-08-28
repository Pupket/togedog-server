package pupket.togedogserver.domain.user.repository.mateRepo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
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

                    return FindMateResponse.builder()
                            .nickname(board.getUser().getNickname())
                            .profileImage(board.getUser().getProfileImage())
                            .gender(EnumMapper.enumToKorean(board.getUser().getUserGender())) // gender 변환
                            .region(EnumMapper.enumToKorean(board.getRegion())) // region 변환
                            .age(LocalDateTime.now().getYear() - board.getUser().getBirthyear())
                            .birth(board.getUser().getBirthyear()+"."+String.valueOf(board.getUser().getBirthday()).substring(0,2)+"."+String.valueOf(board.getUser().getBirthday()).substring(2,4))
                            .accommodatableDogsCount(board.getAccommodatableDogsCount())
                            .career(board.getCareer())
                            .preferred(preferred)
                            .preferredRegion(EnumMapper.enumToKorean(findMate.getPreferredRegion()))
                            .build();
                }).collect(Collectors.toList());

        return new PageImpl<>(MateResponse, pageable, count);
    }
}
