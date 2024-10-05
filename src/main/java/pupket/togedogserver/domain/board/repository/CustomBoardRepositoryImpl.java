package pupket.togedogserver.domain.board.repository;

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
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
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
        /*
            쿼리에 맞춰 값 가져오기
            게시판 테이블과 연관된 반려견 엔티티를 조인을 통해 가져옴
         */

        List<Object[]> results = getBoardRelatedResponse(pageable);

        List<BoardFindResponse> boardResponses = results.stream()
                .collect(Collectors.groupingBy(row -> (Board) row[0])) // Board 기준으로 그룹화
                .entrySet().stream()
                .map(entry -> {
                    Board board = entry.getKey();
                    List<Dog> dogs = entry.getValue().stream()
                            .map(row -> (Dog) row[2])
                            .toList();

                    return createBoardFindResponse(dogs, board);
                })
                .collect(Collectors.toList());

        String countQuery = "SELECT COUNT(b) FROM Board b JOIN b.boardDog bd JOIN bd.dog d WHERE b.deleted = false AND d.deleted = false";
        Long count = em.createQuery(countQuery, Long.class).getSingleResult();

        return new PageImpl<>(boardResponses, pageable, count);
    }

    private List<Object[]> getBoardRelatedResponse(Pageable pageable) {
        String query = "SELECT b, bd, d FROM Board b " +
                "JOIN b.boardDog bd " +
                "JOIN bd.dog d " +
                "WHERE b.deleted = false AND d.deleted = false ORDER BY RAND()";

        TypedQuery<Object[]> result = em.createQuery(query, Object[].class);
        result.setFirstResult((int) pageable.getOffset());
        result.setMaxResults(pageable.getPageSize());
        return result.getResultList();
    }

    @Override
    public Page<BoardFindResponse> findMyBoardList(Long uuid, Pageable pageable) {
        //쿼리에 맞춰 값 가져오기
        TypedQuery<Object[]> result = getBoardRelatedResponse(uuid, pageable);

        List<Object[]> results = result.getResultList();

        // Board 기준으로 그룹화하여 각 Board에 대해 여러 개의 Dog를 처리
        List<BoardFindResponse> boardResponses = results.stream()
                .collect(Collectors.groupingBy(row -> (Board) row[0])) // Board 기준으로 그룹화
                .entrySet().stream()
                .map(entry -> {
                    Board board = entry.getKey();
                    List<Dog> dogs = entry.getValue().stream()
                            .map(row -> (Dog) row[2]) // 각 Board에 속한 Dog들을 수집
                            .toList();

                    return createBoardFindResponse(dogs, board);
                })
                .collect(Collectors.toList());

        //쿼리 결과 카운트 가져오기
        Long count = getCount(uuid);

        return new PageImpl<>(boardResponses, pageable, count);
    }

    private static BoardFindResponse createBoardFindResponse(List<Dog> dogs, Board board) {

        // 개별 Dog 정보를 DogResponse로 매핑
        List<BoardDogResponse> boardDogRespons = dogs.stream()
                .map(dog -> BoardDogResponse.builder()
                        .name(dog.getName())
                        .age(dog.getAge())
                        .breed(dog.getBreed())
                        .dogType(EnumMapper.enumToKorean(dog.getDogType()))
                        .dogGender(dog.getDogGender() ? "수컷" : "암컷")
                        .dogProfileImage(dog.getDogImage())
                        .build())
                .collect(Collectors.toList());

        // Board 정보를 포함한 응답 빌더
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
                .dogs(boardDogRespons) // 여러 마리의 개 정보 추가
                .completeStatus(board.getMatch()==null? CompleteStatus.INCOMPLETE.getStatus() :board.getMatch().getCompleteStatus().getStatus())
                .build();
    }

    private Long getCount(Long uuid) {
        // Count query (그룹화 없이 게시물의 총 개수를 계산)
        String countQuery = "SELECT COUNT(b) FROM Board b " +
                "JOIN b.boardDog bd " +
                "JOIN bd.dog d " +
                "WHERE b.deleted = false AND bd.deleted = false AND d.deleted = false " +
                "AND b.user.uuid = :uuid";

        return em.createQuery(countQuery, Long.class)
                .setParameter("uuid", uuid)
                .getSingleResult();
    }

    private TypedQuery<Object[]> getBoardRelatedResponse(Long uuid, Pageable pageable) {
        String query = "SELECT b, bd, d FROM Board b " +
                "JOIN b.boardDog bd  " +
                "JOIN bd.dog d " +
                "WHERE b.deleted = false AND b.user.uuid = :uuid";

        TypedQuery<Object[]> result = em.createQuery(query, Object[].class);
        result.setParameter("uuid", uuid);
        result.setFirstResult((int) pageable.getOffset());
        result.setMaxResults(pageable.getPageSize());
        return result;
    }
}
