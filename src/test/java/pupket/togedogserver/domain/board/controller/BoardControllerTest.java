package pupket.togedogserver.domain.board.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import pupket.togedogserver.domain.board.constant.FeeType;
import pupket.togedogserver.domain.board.controller.port.BoardService;
import pupket.togedogserver.domain.board.dto.request.BoardCreateRequest;
import pupket.togedogserver.domain.board.dto.request.BoardUpdateRequest;
import pupket.togedogserver.domain.board.dto.response.BoardDogResponse;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.global.security.CustomUserDetail;

@ExtendWith(MockitoExtension.class)
class BoardControllerTest {
    @Mock
    private BoardService boardService;

    @InjectMocks
    private BoardController boardController;

    private CustomUserDetail userDetail;
    private BoardCreateRequest createRequest;
    private BoardUpdateRequest updateRequest;
    private BoardFindResponse findResponse;

    @BeforeEach
    void setUp() {
        userDetail = new CustomUserDetail(
            "testUser",
            "password",
            1L,
            Collections.singletonList(new SimpleGrantedAuthority(RoleType.MEMBER_KAKAO.name()))
        );

        createRequest = BoardCreateRequest.builder()
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

        updateRequest = BoardUpdateRequest.builder()
            .id(1L)
            .title("수정된 게시글")
            .dogIds(List.of(1L))
            .pickUpDay(LocalDate.now())
            .startTime(LocalTime.of(15, 0))
            .endTime(LocalTime.of(17, 0))
            .fee(20000L)
            .feeType(FeeType.PER_HOUR)
            .pickupLocation1("서울시 강남구")
            .tag(Set.of("공원", "한강"))
            .build();

        BoardDogResponse dogResponse = BoardDogResponse.builder()
            .name("테스트견")
            .age(3)
            .breed("포메라니안")
            .dogType("중형견")
            .dogGender("수컷")
            .dogProfileImage("test-image-url")
            .build();

        findResponse = BoardFindResponse.builder()
            .boardId(1L)
            .userId(1L)
            .title("테스트 게시글")
            .pickUpDay(LocalDate.now())
            .fee("10000")
            .feeType(FeeType.PER_HOUR.getFeeType())
            .startTime(LocalTime.of(14, 0).toString())
            .endTime(LocalTime.of(16, 0).toString())
            .pickupLocation1("서울시 강남구")
            .walkingPlaceTag(List.of("공원"))
            .dogs(List.of(dogResponse))
            .completeStatus("미완료")
            .build();
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void create_Success() {
        // Given
        doNothing().when(boardService).create(any(), any());

        // When
        ResponseEntity<Void> response = boardController.create(userDetail, createRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("게시글 조회 성공")
    void find_Success() {
        // Given
        when(boardService.find(any(), anyLong())).thenReturn(findResponse);

        // When
        ResponseEntity<BoardFindResponse> response = boardController.find(userDetail, 1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBoardId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("랜덤 게시글 목록 조회 성공")
    void findRandom_Success() {
        // Given
        Page<BoardFindResponse> expectedPage = new PageImpl<>(List.of(findResponse));
        when(boardService.findRandom(any())).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<BoardFindResponse>> response = boardController.findRandom(0, 5);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void update_Success() {
        // Given
        doNothing().when(boardService).update(any(), any());

        // When
        ResponseEntity<Void> response = boardController.update(userDetail, updateRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void delete_Success() {
        // Given
        doNothing().when(boardService).delete(any(), anyLong());

        // When
        ResponseEntity<Void> response = boardController.delete(userDetail, 1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}