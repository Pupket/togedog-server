package pupket.togedogserver.domain.board.repository;

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
import pupket.togedogserver.global.mapper.EnumMapper;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class CustomBoardRepositoryImpl implements CustomBoardRepository {

    private final EntityManager em;

    @Override
    public Page<BoardFindResponse> BoardList(Pageable pageable) {
        // JPQL query to select fields from Board and Dog
        String query = "SELECT b, d FROM Board b " +
                "JOIN b.dog d " +
                "WHERE b.deleted = false AND d.deleted = false";

        TypedQuery<Object[]> result = em.createQuery(query, Object[].class);
        result.setFirstResult((int) pageable.getOffset());
        result.setMaxResults(pageable.getPageSize());

        List<Object[]> results = result.getResultList();
        List<BoardFindResponse> boardResponses = results.stream()
                .map(row -> {
                    Board board = (Board) row[0];
                    Dog dog = (Dog) row[1];

                    return BoardFindResponse.builder()
                            .title(board.getTitle())
                            .pickUpDay(board.getPickUpDay())
                            .fee(EnumMapper.enumToKorean(board.getFee()))
                            .startTime(String.valueOf(board.getStartTime()))
                            .endTime(String.valueOf(board.getEndTime()))
                            .pickupLocation2(board.getPickupLocation2())
                            .walkingPlaceTag(board.getWalkingPlaceTag().stream()
                                    .map(WalkingPlaceTag::getPlaceName)
                                    .collect(Collectors.toList()))
                            .name(dog.getName())
                            .age(dog.getAge())
                            .dogType(EnumMapper.enumToKorean(dog.getBreed()))
                            .build();
                })
                .collect(Collectors.toList());

        // Count query for pagination
        String countQuery = "SELECT COUNT(b) FROM Board b JOIN b.dog d WHERE b.deleted = false AND d.deleted = false";
        Long count = em.createQuery(countQuery, Long.class).getSingleResult();

        return new PageImpl<>(boardResponses, pageable, count);
    }
}
