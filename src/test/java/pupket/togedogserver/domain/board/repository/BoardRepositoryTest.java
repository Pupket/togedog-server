package pupket.togedogserver.domain.board.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pupket.togedogserver.domain.board.constant.FeeType;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.board.repository.jpaRepositry.BoardDogJPARepository;
import pupket.togedogserver.domain.board.repository.jpaRepositry.BoardJPARepository;
import pupket.togedogserver.domain.board.repository.jpaRepositry.WalkingPlaceTagJPARepository;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
class BoardRepositoryTest {
    @Mock
    private BoardJPARepository boardJPARepository;
    @Mock
    private BoardDogJPARepository boardDogJPARepository;
    @Mock
    private WalkingPlaceTagJPARepository walkingPlaceTagJPARepository;

    @InjectMocks
    private BoardRepositoryImpl boardRepository;
    @InjectMocks
    private BoardDogRepositoryImpl boardDogRepository;
    @InjectMocks
    private WalkingPlaceTagRepositoryImpl walkingPlaceTagRepository;

    private User user;
    private Dog dog;
    private Board board;
    private BoardDog boardDog;
    private WalkingPlaceTag walkingPlaceTag;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .uuid(1L)
            .email("test@test.com")
            .name("tester")
            .build();

        dog = Dog.builder()
            .dogId(1L)
            .name("테스트견")
            .age(3)
            .user(user)
            .dogGender(true)
            .dogType(DogType.MID)
            .breed("포메라니안")
            .dogImage("test-image-url")
            .build();

        walkingPlaceTag = WalkingPlaceTag.builder()
            .id(1L)
            .placeName("공원")
            .build();

        board = Board.builder()
            .boardId(1L)
            .user(user)
            .title("테스트 게시글")
            .pickUpDay(LocalDate.now())
            .startTime(LocalTime.of(14, 0))
            .endTime(LocalTime.of(16, 0))
            .fee(10000L)
            .feeType(FeeType.PER_HOUR)
            .pickupLocation1("서울시 강남구")
            .walkingPlaceTag(Set.of(walkingPlaceTag))
            .build();

        boardDog = BoardDog.builder()
            .boardDogId(1L)
            .board(board)
            .dog(dog)
            .build();

        board.setBoardDog(List.of(boardDog));
        walkingPlaceTag.setBoard(board);
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공")
    void findByBoardId_Success() {
        // Given
        when(boardJPARepository.findByBoardId(any())).thenReturn(Optional.of(board));

        // When
        Optional<Board> result = boardRepository.findByBoardId(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getBoardId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자와 게시글 ID로 조회 성공")
    void findByUserAndBoardId_Success() {
        // Given
        when(boardJPARepository.findByUserAndBoardId(any(), any())).thenReturn(Optional.of(board));

        // When
        Optional<Board> result = boardRepository.findByUserAndBoardId(user, 1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("게시글 저장 성공")
    void save_Success() {
        // Given
        when(boardJPARepository.save(any())).thenReturn(board);

        // When
        Board result = boardRepository.save(board);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBoardId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("BoardDog 저장 성공")
    void saveBoardDog_Success() {
        // Given
        when(boardDogJPARepository.save(any())).thenReturn(boardDog);

        // When
        BoardDog result = boardDogRepository.save(boardDog);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBoardDogId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("BoardDog 일괄 저장 성공")
    void saveAllBoardDog_Success() {
        // Given
        List<BoardDog> boardDogs = List.of(boardDog);
        when(boardDogJPARepository.saveAll(any())).thenReturn(boardDogs);

        // When
        List<BoardDog> results = boardDogRepository.saveAll(boardDogs);

        // Then
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("WalkingPlaceTag 조회 성공")
    void findAllByBoard_Success() {
        // Given
        List<WalkingPlaceTag> tags = List.of(walkingPlaceTag);
        when(walkingPlaceTagJPARepository.findAllByBoard(any())).thenReturn(Optional.of(tags));

        // When
        Optional<List<WalkingPlaceTag>> results = walkingPlaceTagRepository.findAllByBoard(board);

        // Then
        assertThat(results).isPresent();
        assertThat(results.get()).hasSize(1);
    }

    @Test
    @DisplayName("WalkingPlaceTag 저장 성공")
    void saveWalkingPlaceTag_Success() {
        // Given
        when(walkingPlaceTagJPARepository.save(any())).thenReturn(walkingPlaceTag);

        // When
        WalkingPlaceTag result = walkingPlaceTagRepository.save(walkingPlaceTag);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPlaceName()).isEqualTo("공원");
    }

    @Test
    @DisplayName("WalkingPlaceTag 일괄 삭제 성공")
    void deleteAllWalkingPlaceTag_Success() {
        // Given
        List<WalkingPlaceTag> tags = List.of(walkingPlaceTag);

        // When
        walkingPlaceTagRepository.deleteAll(tags);

        // Then
        verify(walkingPlaceTagJPARepository).deleteAll(tags);
    }
}