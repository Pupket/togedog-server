package pupket.togedogserver.domain.board.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CustomBoardRepositoryImpl implements CustomBoardRepository {

    private final EntityManager em;

    @Override
    public Page<BoardFindResponse> BoardList(Pageable pageable) {
        String query = "SELECT b, d FROM Board b " +
                "JOIN b.dog d " +
                "WHERE b.deleted = false AND d.deleted = false order by rand()";

        TypedQuery<Object[]> result = em.createQuery(query, Object[].class);
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
                            .breed(dog.getBreed())
                            .dogType(EnumMapper.enumToKorean(dog.getDogType()))
                            .dogGender(dog.getDogGender()?"수컷":"암컷")
                            .dogProfileImage(dog.getDogImage())
                            .build();
                })
                .collect(Collectors.toList());

        String countQuery = "SELECT COUNT(b) FROM Board b JOIN b.dog d WHERE b.deleted = false AND d.deleted = false";
        Long count = em.createQuery(countQuery, Long.class).getSingleResult();

        return new PageImpl<>(boardResponses, pageable, count);
    }

    @Override
    public Page<BoardFindResponse> findMyBoardList(Long uuid, Pageable pageable) {
        String query = "SELECT b, d FROM Board b " +
                "JOIN Dog d ON b.dog = d " +
                "WHERE b.deleted = false AND d.deleted = false " +
                "AND b.user.uuid = :uuid";

        TypedQuery<Object[]> result = em.createQuery(query, Object[].class);
        result.setParameter("uuid", uuid); // uuid 파라미터에 값 설정
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
                            .breed(dog.getBreed())
                            .dogType(EnumMapper.enumToKorean(dog.getDogType()))
                            .dogGender(dog.getDogGender()?"수컷":"암컷")
                            .dogProfileImage(dog.getDogImage())
                            .build();
                })
                .collect(Collectors.toList());

        String countQuery = "SELECT COUNT(b) FROM Board b JOIN Dog d ON b.dog = d WHERE b.deleted = false AND d.deleted = false AND b.user.uuid = :uuid";
        Long count = em.createQuery(countQuery, Long.class)
                .setParameter("uuid", uuid)
                .getSingleResult();

        return new PageImpl<>(boardResponses, pageable, count);
    }

}
