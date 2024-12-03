package pupket.togedogserver.domain.dog.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import pupket.togedogserver.domain.dog.controller.port.DogService;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.global.security.CustomUserDetail;

@ExtendWith(MockitoExtension.class)
class DogControllerTest {
    @Mock
    private DogService dogService;

    @InjectMocks
    private DogController dogController;

    private CustomUserDetail userDetail;
    private ObjectMapper objectMapper;
    private MockMultipartFile mockProfileImage;
    private DogResponse dogResponse;

    @BeforeEach
    void setUp() {
        userDetail = new CustomUserDetail(
            "testUser",
            "password",
            1L,
            Collections.singletonList(new SimpleGrantedAuthority(RoleType.MEMBER_KAKAO.name()))
        );

        objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .findAndAddModules()
            .build();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mockProfileImage = new MockMultipartFile(
            "profileImage",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        dogResponse = DogResponse.builder()
            .dogId(1L)
            .name("테스트견")
            .age(3)
            .dogGender(true)
            .breed("포메라니안")
            .dogType("중형견")
            .weight(10L)
            .neutered(true)
            .region("서울")
            .notes("테스트 메모")
            .dogImage("test-image-url")
            .dogPersonalityTags(Set.of("활발한", "친절한"))
            .build();
    }

    @Test
    @DisplayName("강아지 등록 성공")
    void create_Success() throws Exception {
        // Given
        String requestString = "{" +
            "\"name\": \"테스트견\"," +
            "\"age\": 3," +
            "\"dogGender\": true," +
            "\"breed\": \"포메라니안\"," +
            "\"weight\": 10," +
            "\"neutered\": true," +
            "\"region\": \"서울\"," +
            "\"notes\": \"테스트 메모\"," +
            "\"tags\": [\"활발한\", \"친절한\"]," +
            "\"vaccine\": true" +
            "}";

        doNothing().when(dogService).create(any(), any(), any());

        // When
        ResponseEntity<Void> response = dogController.create(userDetail, requestString, mockProfileImage);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("강아지 조회 성공")
    void find_Success() {
        // Given
        when(dogService.find(any(), anyLong())).thenReturn(dogResponse);

        // When
        ResponseEntity<DogResponse> response = dogController.find(userDetail, 1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("테스트견");
    }

    @Test
    @DisplayName("강아지 전체 조회 성공")
    void findAll_Success() {
        // Given
        when(dogService.findAll(any())).thenReturn(List.of(dogResponse));

        // When
        ResponseEntity<List<DogResponse>> response = dogController.findAll(userDetail);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("랜덤 강아지 목록 조회 성공")
    void findRandom_Success() {
        // Given
        Page<DogResponse> expectedPage = new PageImpl<>(List.of(dogResponse));
        when(dogService.findRandom(any())).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<DogResponse>> response = dogController.findRandom(0, 5);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("강아지 수정 성공")
    void update_Success() throws Exception {
        // Given
        String requestString = "{" +
            "\"id\": 1," +
            "\"name\": \"수정된견\"," +
            "\"age\": 4," +
            "\"breed\": \"말티즈\"," +
            "\"weight\": 8," +
            "\"neutered\": true," +
            "\"region\": \"경기\"," +
            "\"notes\": \"수정된 메모\"," +
            "\"tags\": [\"온순한\"]," +
            "\"vaccine\": false" +
            "}";

        doNothing().when(dogService).update(any(), any(), any());

        // When
        ResponseEntity<Void> response = dogController.update(userDetail, requestString, mockProfileImage);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("강아지 삭제 성공")
    void delete_Success() {
        // Given
        doNothing().when(dogService).delete(any(), anyLong());

        // When
        ResponseEntity<Void> response = dogController.delete(userDetail, 1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("견종 자동완성 검색 성공")
    void autoCompleteKeyword_Success() {
        // Given
        List<String> expectedResults = List.of("포메라니안", "포메");
        when(dogService.autoCompleteKeyword(any())).thenReturn(expectedResults);

        // When
        ResponseEntity<List<String>> response = dogController.autoCompleteKeyword("포메");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }
}