package pupket.togedogserver.domain.user.repository.mateRepo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class CustomMateRepositoryImpl implements CustomMateRepository {

    private final EntityManager em;

    @Override
    public Page<FindMateResponse> MateList(Pageable pageable) {
        String query = "SELECT b FROM Mate b WHERE b.deleted = false";
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
                            return FindMateResponse.builder()
                                    .nickname(board.getUser().getNickname())
                                    .profileImage(board.getUser().getProfileImage())
                                    .gender(String.valueOf(board.getUser().getUserGender()))
                                    .region(String.valueOf(board.getRegion()))
                                    .age(LocalDateTime.now().getYear() - board.getUser().getBirthyear())
                                    .accommodatableDogsCount(board.getAccommodatableDogsCount())
                                    .career(board.getCareer())
                                    .preferredStyle(board.getMateTags().stream().map(tag -> tag.getTagName()).collect(Collectors.toSet()))
                                    .preferredTime(board.getPreferredTimes().stream().map(time -> time.getPreferredTime().toString()).collect(Collectors.toSet()))
                                    .preferredWeek(board.getPreferredWeeks().stream().map(week -> week.getPreferredWeek().toString()).collect(Collectors.toSet()))
                                    .preferredBreed(board.getPreferredBreeds().stream().map(breed -> breed.getPreferredBreed().toString()).collect(Collectors.toSet()))
                                    .build();
                        }
                ).collect(Collectors.toList());

        return new PageImpl<>(MateResponse, pageable, count);

    }
}

