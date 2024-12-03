package pupket.togedogserver.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;

import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.service.BoardServiceImpl;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.match.service.MatchServiceImpl;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.constant.Time;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.constant.Week;
import pupket.togedogserver.domain.user.controller.port.MateService;
import pupket.togedogserver.domain.user.dto.request.Preferred;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.dto.response.PreferredDetailsResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

@ExtendWith(MockitoExtension.class)
class MateControllerTest {
    @Mock
    private MateService mateService;
    @Mock
    private BoardServiceImpl boardService;
    @Mock
    private MatchServiceImpl matchService;
    
    @InjectMocks
    private MateController mateController;

    private CustomUserDetail userDetail;
    private ObjectMapper objectMapper;
    private MockMultipartFile mockProfileImage;

    @BeforeEach
    void setUp() {
        userDetail = new CustomUserDetail(
            "testUser",
            "password",
            1L,
            Collections.singletonList(new SimpleGrantedAuthority(RoleType.MEMBER_KAKAO.name()))
        );

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mockProfileImage = new MockMultipartFile(
            "profileImage",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("메이트 생성 성공")
    void create_Success() throws Exception {
        // Given
        String requestString = """
            {
                "nickname": "newTester",
                "userGender": "%s",
                "phoneNumber": "01012345678",
                "accommodatableDogsCount": 2,
                "career": "신규 경력",
                "birthday": "19900101",
                "preferredDetails": {
                    "region": "%s",
                    "dogTypes": ["%s", "%s"],
                    "times": ["%s", "%s"],
                    "weeks": ["%s", "%s"],
                    "hashTag": ["친절한", "활발한"]
                }
            }
            """.formatted(
            UserGender.MALE.getGender(),
            Region.SEOUL.getRegion(),
            DogType.MID.getBreed(),
            DogType.BIG.getBreed(),
            Time.MORNING.getTime(),
            Time.AFTERNOON.getTime(),
            Week.TUE.getWeek(),
            Week.FRI.getWeek()
        );

        doNothing().when(mateService).create(any(), any(), any());

        // When
        ResponseEntity<Void> response = mateController.create(userDetail, requestString, mockProfileImage);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("메이트 조회 성공")
    void find_Success() {
        // Given
        FindMateResponse expectedResponse = createMockFindMateResponse();
        when(mateService.find(any())).thenReturn(expectedResponse);

        // When
        ResponseEntity<FindMateResponse> response = mateController.find(userDetail);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("랜덤 메이트 조회 성공")
    void findRandom_Success() {
        // Given
        Page<FindMateResponse> expectedPage = new PageImpl<>(Collections.emptyList());
        when(mateService.findRandom(any())).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<FindMateResponse>> response = mateController.findRandom(0, 4);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("메이트 삭제 성공")
    void delete_Success() {
        // Given
        doNothing().when(mateService).delete(any());

        // When
        ResponseEntity<Void> response = mateController.delete(userDetail);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("닉네임 중복 체크 성공")
    void checkNickname_Success() {
        // Given
        String nickname = "testNick";
        when(mateService.checkNickname(any(), anyString())).thenReturn(true);

        // When
        ResponseEntity<HashMap<String,Object>> response = mateController.checkNickName(userDetail, nickname);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("flag", "true");
    }

    @Test
    @DisplayName("자동완성 키워드 검색 성공")
    void autoCompleteKeyword_Success() {
        // Given
        String keyword = "test";
        List<String> expectedResults = List.of("test", "tester");
        when(mateService.autoCompleteKeyword(anyString())).thenReturn(expectedResults);

        // When
        ResponseEntity<List<String>> response = mateController.autoCompleteKeyword(keyword);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(2);
    }

    private RegistMateRequest createRegistMateRequest() {
        Preferred preferred = new Preferred();
        preferred.setRegion(Region.SEOUL);
        preferred.setDogTypes(Set.of(DogType.MID, DogType.BIG));
        preferred.setTimes(Set.of(Time.MORNING, Time.AFTERNOON));
        preferred.setWeeks(Set.of(Week.TUE, Week.FRI));
        preferred.setHashTag(Set.of("친절한", "활발한"));

        return RegistMateRequest.builder()
            .nickname("newTester")
            .userGender(UserGender.MALE)
            .phoneNumber("01012345678")
            .birthday(String.valueOf(19900101))
            .career("신규 경력")
            .preferredDetails(preferred)
            .build();
    }

    private FindMateResponse createMockFindMateResponse() {
        PreferredDetailsResponse preferred = PreferredDetailsResponse.builder()
            .week(Set.of("월요일", "화요일"))
            .time(Set.of("아침", "오후"))
            .hashTag(Set.of("친절한"))
            .breed(Set.of("중형견"))
            .region("서울")
            .build();

        return FindMateResponse.builder()
            .uuid(1L)
            .mateId(1L)
            .nickname("testNick")
            .gender("남성")
            .age(30)
            .birth("1990.01.01")
            .accommodatableDogsCount(2)
            .career("테스트 경력")
            .preferred(preferred)
            .build();
    }
}