package pupket.togedogserver.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.repository.jpaRepositry.BoardJPARepository;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.domain.board.repository.CustomBoardRepositoryImpl;
import pupket.togedogserver.domain.user.dto.response.FindMatchedScheduleResponse;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.customException.BoardException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.domain.board.constant.FeeType;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;

@ExtendWith(MockitoExtension.class)
class OwnerServiceImplTest {
    @Mock
    private CustomBoardRepositoryImpl customBoardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BoardJPARepository boardJPARepository;
    @InjectMocks
    private OwnerServiceImpl ownerService;

    private CustomUserDetail userDetail;
    private User user;
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
            .nickname("tester")
            .role(RoleType.MEMBER_KAKAO)
            .build();

        mate = Mate.builder()
            .mateUuid(1L)
            .user(user)
            .matchCount(0L)
            .career("테스트 경력")
            .accommodatableDogsCount(2)
            .preferredRegion(Region.SEOUL)
            .build();

        board = Board.builder()
            .boardId(1L)
            .user(user)
            .title("테스트 게시글")
            .matched(MatchStatus.MATCHED)
            .pickUpDay(LocalDate.now())
            .startTime(LocalTime.of(14, 0))
            .endTime(LocalTime.of(16, 0))
            .fee(10000L)
            .feeType(FeeType.PER_HOUR)
            .pickupLocation1("서울시 강남구")
            .deleted(false)
            .build();
    }

    @Test
    @DisplayName("내 게시글 목록 조회 성공")
    void findMyBoards_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<BoardFindResponse> expectedPage = new PageImpl<>(Collections.emptyList());
        
        when(customBoardRepository.findMyBoardList(userDetail.getUuid(), pageable))
            .thenReturn(expectedPage);

        // When
        Page<BoardFindResponse> result = ownerService.findMyBoards(userDetail, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(customBoardRepository).findMyBoardList(userDetail.getUuid(), pageable);
    }

    @Test
    @DisplayName("내 스케줄 조회 성공")
    void findMySchedule_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Match match = Match.builder()
            .matchId(1L)
            .mate(mate)
            .board(board)
            .build();
        board.setMatch(match);
        
        when(userRepository.findByUuid(userDetail.getUuid())).thenReturn(Optional.of(user));
        when(boardJPARepository.findByUser(user)).thenReturn(Optional.of(List.of(board)));

        // When
        Page<FindMatchedScheduleResponse> result = ownerService.findMySchedule(userDetail, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("내 스케줄 조회 실패 - 존재하지 않는 사용자")
    void findMySchedule_UserNotFound() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByUuid(userDetail.getUuid())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ownerService.findMySchedule(userDetail, pageable))
            .isInstanceOf(MemberException.class)
            .hasMessageContaining("Member not found");
    }

    @Test
    @DisplayName("내 스케줄 조회 실패 - 게시글 없음")
    void findMySchedule_BoardNotFound() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByUuid(userDetail.getUuid())).thenReturn(Optional.of(user));
        when(boardJPARepository.findByUser(user)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ownerService.findMySchedule(userDetail, pageable))
            .isInstanceOf(BoardException.class)
            .hasMessageContaining("Board not found");
    }

    @Test
    @DisplayName("매칭되지 않은 게시글 필터링 확인")
    void findMySchedule_FilterUnmatchedBoards() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Board unmatchedBoard = Board.builder()
            .boardId(2L)
            .user(user)
            .title("미매칭 게시글")
            .matched(MatchStatus.UNMATCHED)
            .build();
        
        when(userRepository.findByUuid(userDetail.getUuid())).thenReturn(Optional.of(user));
        when(boardJPARepository.findByUser(user)).thenReturn(Optional.of(List.of(unmatchedBoard)));

        // When
        Page<FindMatchedScheduleResponse> result = ownerService.findMySchedule(userDetail, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
    }
}