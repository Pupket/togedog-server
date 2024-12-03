package pupket.togedogserver.domain.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import pupket.togedogserver.domain.board.dto.request.BoardCreateRequest;
import pupket.togedogserver.domain.board.dto.request.BoardUpdateRequest;
import pupket.togedogserver.domain.board.dto.response.BoardDogResponse;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.board.mapper.BoardMapper;
import pupket.togedogserver.domain.board.service.port.BoardDogRepository;
import pupket.togedogserver.domain.board.service.port.BoardRepository;
import pupket.togedogserver.domain.board.service.port.CustomBoardRepository;
import pupket.togedogserver.domain.board.service.port.WalkingPlaceTagRepository;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.repository.jpaRepository.DogJPARepository;
import pupket.togedogserver.domain.token.entity.RefreshToken;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.service.port.CustomMateRepository;
import pupket.togedogserver.domain.user.service.port.MateRepository;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.customException.BoardException;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.domain.board.constant.FeeType;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalkingPlaceTagRepository walkingPlaceTagRepository;
    @Mock
    private BoardMapper boardMapper;
    @Mock
    private DogJPARepository dogJPARepository;
    @Mock
    private CustomBoardRepository customBoardRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private CustomMateRepository customMateRepository;
    @Mock
    private MateRepository mateRepository;
    @Mock
    private BoardDogRepository boardDogRepository;

    @InjectMocks
    private BoardServiceImpl boardService;

    private CustomUserDetail userDetail;
    private User user;
    private Dog dog;
    private Board board;
    private Mate mate;

    @BeforeEach
    void setUp() {
        userDetail = new CustomUserDetail(
            "testUser",
            "password",
            1L,
            Collections.singletonList(new SimpleGrantedAuthority(RoleType.MEMBER_KAKAO.name()))
        );

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

        BoardDog boardDog = BoardDog.builder()
            .boardDogId(1L)
            .dog(dog)
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
            .walkingPlaceTag(Set.of(WalkingPlaceTag.builder()
                .id(1L)
                .placeName("공원")
                .build()))
            .boardDog(List.of(boardDog))
            .build();

        mate = Mate.builder()
            .mateUuid(1L)
            .user(user)
            .build();

        boardDog.setBoard(board);
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void create_Success() {
        // Given
        BoardCreateRequest request = createBoardCreateRequest();
        when(userRepository.findByUuid(anyLong())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.getRefreshTokenByMemberId(anyLong()))
            .thenReturn(Optional.of(RefreshToken.builder()
                .refreshToken("test-token")
                .memberId(1L)
                .build()));
        when(dogJPARepository.findById(anyLong())).thenReturn(Optional.of(dog));
        when(boardMapper.toBoard(any())).thenReturn(board);
        when(boardRepository.save(any())).thenReturn(board);

        // When
        boardService.create(userDetail, request);

        // Then
        verify(boardRepository, times(2)).save(any(Board.class));
    }

    @Test
    @DisplayName("게시글 조회 성공")
    void find_Success() {
        // Given
        when(userRepository.findByUuid(anyLong())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.getRefreshTokenByMemberId(anyLong()))
            .thenReturn(Optional.of(RefreshToken.builder()
                .refreshToken("test-token")
                .memberId(1L)
                .build()));
        when(boardRepository.findByBoardId(anyLong())).thenReturn(Optional.of(board));

        // When
        BoardFindResponse response = boardService.find(userDetail, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBoardId()).isEqualTo(1L);
        assertThat(response.getDogs()).hasSize(1);
    }

    @Test
    @DisplayName("랜덤 게시글 목록 조회 성공")
    void findRandom_Success() {
        // Given
        Page<BoardFindResponse> expectedPage = new PageImpl<>(Collections.emptyList());
        when(customBoardRepository.BoardList(any())).thenReturn(expectedPage);

        // When
        Page<BoardFindResponse> result = boardService.findRandom(PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("내 일정 조회 성공")
    void findMySchedule_Success() {
        // Given
        when(userRepository.findByUuid(anyLong())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.getRefreshTokenByMemberId(anyLong()))
            .thenReturn(Optional.of(RefreshToken.builder()
                .refreshToken("test-token")
                .memberId(1L)
                .build()));
        when(mateRepository.findByUser(any())).thenReturn(Optional.of(mate));
        when(customMateRepository.findMyScheduleList(anyLong(), any()))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When
        Page<BoardFindResponse> result = boardService.findMySchedule(userDetail, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
    }

    private BoardCreateRequest createBoardCreateRequest() {
        return BoardCreateRequest.builder()
            .title("테스트 게시글")
            .dogIds(List.of(1L))
            .pickUpDay(LocalDate.now())
            .startTime(LocalTime.of(14, 0))
            .endTime(LocalTime.of(16, 0))
            .fee(10000)
            .feeType(FeeType.PER_HOUR)
            .pickupLocation1("서울시 강남구")
            .tag(Set.of("공원"))
            .build();
    }
}