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
import pupket.togedogserver.domain.board.service.port.CustomBoardRepository;
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
    public Page<BoardFindResponse> findRandomBoardList(Pageable pageable) {
        String query = "SELECT DISTINCT b, bd, d FROM Board b " +
                "JOIN FETCH b.boardDog bd " +
                "JOIN FETCH bd.dog d " +
                "WHERE b.deleted = false AND d.deleted = false " +
                "ORDER BY RAND()";

        TypedQuery<Object[]> result = em.createQuery(query, Object[].class);
        result.setFirstResult((int) pageable.getOffset());
        result.setMaxResults(pageable.getPageSize());

        List<Object[]> results = result.getResultList();

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
                .toList();

        String countQuery = "SELECT COUNT(b) FROM Board b JOIN b.boardDog bd JOIN bd.dog d WHERE b.deleted = false AND d.deleted = false";
        Long count = em.createQuery(countQuery, Long.class).getSingleResult();

        return new PageImpl<>(boardResponses, pageable, count);
    }

    @Override
    public Page<BoardFindResponse> findMyBoardList(Long uuid, Pageable pageable) {
        // 1. 먼저 Board ID만 페이징해서 조회
        String boardIdQuery = "SELECT b.boardId FROM Board b " +
                "WHERE b.user.uuid = :uuid AND b.deleted = false";
        
        TypedQuery<Long> boardIdResult = em.createQuery(boardIdQuery, Long.class)
                .setParameter("uuid", uuid)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());
        
        List<Long> boardIds = boardIdResult.getResultList();
        
        // 2. 조회된 ID로 실제 데이터를 FetchJoin으로 조회
        String detailQuery = "SELECT DISTINCT b, bd, d FROM Board b " +
                "JOIN FETCH b.boardDog bd " +
                "JOIN FETCH bd.dog d " +
                "WHERE b.boardId IN :boardIds";
        
        List<Object[]> results = em.createQuery(detailQuery, Object[].class)
                .setParameter("boardIds", boardIds)
                .getResultList();
        
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
                .toList();

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
                .toList();

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
                        .toList())
                .dogs(boardDogRespons) // 여러 마리의 개 정보 추가
                .completeStatus(getCompleteStatus(board))
                .build();
    }

    private static String getCompleteStatus(Board board) {
        // Null 체크 및 비어 있는 경우 처리
        if (board.getMatch() == null || board.getMatch().isEmpty()) {
            return CompleteStatus.INCOMPLETE.getStatus();
        }

        // 하나라도 COMPLETE 상태가 아니라면 INCOMPLETE 반환
        boolean hasIncomplete = board.getMatch().stream()
                .anyMatch(match -> !match.getCompleteStatus().equals(CompleteStatus.COMPLETE.getStatus()));

        return hasIncomplete ? CompleteStatus.INCOMPLETE.getStatus() : CompleteStatus.COMPLETE.getStatus();
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
}
