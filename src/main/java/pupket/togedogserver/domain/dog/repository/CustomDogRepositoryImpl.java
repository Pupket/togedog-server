package pupket.togedogserver.domain.dog.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.user.dto.response.DogActiveResponse;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomDogRepositoryImpl implements CustomDogRepository {

    private final EntityManager em;

    @Override
    public Page<DogResponse> dogList(Pageable pageable) {
        //쿼리로 반려견 엔티티리스트 검색
        List<Dog> results = getDogList(pageable);

        //쿼리로 쿼리 결과 갯수 반환
        Long count = getCount();

        //반환할 dto 맵핑 및 생성
        List<DogResponse> dogResponse = getDogResponseList(results);

        return new PageImpl<>(dogResponse, pageable, count);
    }

    private static List<DogResponse> getDogResponseList(List<Dog> results) {
        return results.stream()
                .map(dog -> {
                    Set<String> list = new HashSet<>();
                    dog.getDogPersonalityTags().forEach(
                            tag -> list.add(tag.getTag())
                    );

                    return DogResponse.builder()
                            .dogId(dog.getDogId())
                            .name(dog.getName())
                            .dogType(EnumMapper.enumToKorean(dog.getDogType()))
                            .breed(dog.getBreed())
                            .dogGender(dog.getDogGender().equals(true))
                            .neutered(dog.getNeutered())
                            .weight(dog.getWeight())
                            .notes(dog.getNotes())
                            .dogImage(dog.getDogImage())
                            .vaccine(dog.isVaccine())
                            .age(dog.getAge())
                            .dogPersonalityTags(list)
                            .region(EnumMapper.enumToKorean(dog.getRegion()))
                            .build();
                }).toList();
    }

    private Long getCount() {
        String countJpql = "SELECT COUNT(b) FROM Mate b WHERE b.deleted = false";
        return em.createQuery(countJpql, Long.class).getSingleResult();
    }

    private List<Dog> getDogList(Pageable pageable) {
        String query = "SELECT d FROM Dog d" +
                " WHERE d.deleted = false " +
                "order by rand()";

        TypedQuery<Dog> result = em.createQuery(query, Dog.class);

        result.setFirstResult((int) pageable.getOffset());
        result.setMaxResults(pageable.getPageSize());

        return result.getResultList();
    }

    public DogActiveResponse findDogActions(Long Uuid) {

        List<Board> resultList = getBoardList(Uuid);

        //산책 시간 결과 반환
        AtomicLong durationSum = getDurationSum(resultList);
        //연관 반려견중 대표로 한 마리 출력
        String dogName = resultList.get(0).getBoardDog().get(0).getDog().getName();

        Long count = getCount(Uuid);

        return DogActiveResponse.builder()
                .name(dogName)
                .walkTime(durationSum.get())
                .walkCount(count)
                .build();
    }

    private static AtomicLong getDurationSum(List<Board> resultList) {
        AtomicLong durationSum = new AtomicLong(0L);
        resultList.forEach(
                i -> {
                    LocalTime startTime = i.getStartTime();
                    LocalTime endTime = i.getEndTime();

                    long duration = Duration.between(startTime, endTime).toHours();
                    durationSum.addAndGet(duration);
                }
        );
        return durationSum;
    }

    private Long getCount(Long Uuid) {
        String countQuery = "select count(b) from Board b " +
                " join BoardDog bd" +
                "        on b.boardId = bd.boardDogId" +
                "    join Dog d" +
                "        on bd.dog.dogId = d.dogId " +
                "Where b.user.uuid = :Uuid";
        return em.createQuery(countQuery, Long.class)
                .setParameter("Uuid", Uuid)
                .getSingleResult();
    }

    private List<Board> getBoardList(Long Uuid) {
        String query = "select distinct(b) from Board b " +
                " join BoardDog bd" +
                "        on b.boardId = bd.boardDogId" +
                "    join Dog d" +
                "        on bd.dog.dogId = d.dogId " +
                "Where b.user.uuid = :Uuid";

        TypedQuery<Board> result = em.createQuery(query, Board.class);
        result.setParameter("Uuid", Uuid);

        return result.getResultList();
    }
}
