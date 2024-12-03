package pupket.togedogserver.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import pupket.togedogserver.domain.board.constant.FeeType;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.controller.port.OwnerService;
import pupket.togedogserver.domain.user.dto.response.FindMatchedScheduleResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {
    @Mock
    private OwnerService ownerService;
    
    @InjectMocks
    private OwnerController ownerController;

    private CustomUserDetail userDetail;

    @BeforeEach
    void setUp() {
        userDetail = new CustomUserDetail(
            "testUser",
            "password",
            1L,
            Collections.singletonList(new SimpleGrantedAuthority(RoleType.MEMBER_KAKAO.name()))
        );
    }

    @Test
    @DisplayName("내 산책 게시글 리스트 조회 성공")
    void findMyBoards_Success() {
        // Given
        BoardFindResponse boardResponse = createMockBoardResponse();
        Page<BoardFindResponse> expectedPage = new PageImpl<>(List.of(boardResponse));
        when(ownerService.findMyBoards(any(), any())).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<BoardFindResponse>> response = ownerController.findMyBoards(userDetail, 0, 5, false);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("내 산책 일정 리스트 조회 성공")
    void findMySchedules_Success() {
        // Given
        FindMatchedScheduleResponse scheduleResponse = createMockScheduleResponse();
        PageImpl<FindMatchedScheduleResponse> expectedPage = new PageImpl<>(List.of(scheduleResponse));
        when(ownerService.findMySchedule(any(), any())).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<FindMatchedScheduleResponse>> response = ownerController.findMySchedules(userDetail, 0, 5, false);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("첫 요청시 페이지 사이즈 10으로 설정")
    void findMyBoards_FirstRequest_Success() {
        // Given
        BoardFindResponse boardResponse = createMockBoardResponse();
        Page<BoardFindResponse> expectedPage = new PageImpl<>(List.of(boardResponse));
        when(ownerService.findMyBoards(any(), any())).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<BoardFindResponse>> response = ownerController.findMyBoards(userDetail, 0, 5, true);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    private BoardFindResponse createMockBoardResponse() {
        return BoardFindResponse.builder()
            .boardId(1L)
            .userId(1L)
            .title("테스트 게시글")
            .pickUpDay(LocalDate.now())
            .fee(10000L+"")
            .feeType(FeeType.PER_HOUR.getFeeType())
            .startTime(LocalTime.of(14, 0).toString())
            .endTime(LocalTime.of(16, 0).toString())
            .pickupLocation1("서울시 강남구")
            .walkingPlaceTag(List.of("공원"))
            .dogs(Collections.emptyList())
            .completeStatus(CompleteStatus.INCOMPLETE.getStatus())
            .build();
    }

    private FindMatchedScheduleResponse createMockScheduleResponse() {
        return FindMatchedScheduleResponse.builder()
            .boardId(1L)
            .pickUpDay(LocalDate.now().toString())
            .startTime(LocalTime.of(14, 0).toString())
            .endTime(LocalTime.of(16, 0).toString())
            .fee("10000")
            .feeType(FeeType.PER_HOUR.toString())
            .mateNickname("testMate")
            .matePhotoUrl("test-photo-url")
            .mateId(1L)
            .matchStatus("매칭완료")
            .completeStatus("미완료")
            .build();
    }
}