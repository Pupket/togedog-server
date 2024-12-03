package pupket.togedogserver.domain.dog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.entity.DogPersonalityTag;
import pupket.togedogserver.domain.dog.mapper.DogMapper;
import pupket.togedogserver.domain.dog.repository.CustomDogRepositoryImpl;
import pupket.togedogserver.domain.dog.service.port.DogPersonalityTagRepository;
import pupket.togedogserver.domain.dog.service.port.DogRepository;
import pupket.togedogserver.domain.token.entity.RefreshToken;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.service.port.OwnerRepository;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.customException.DogException;
import pupket.togedogserver.global.redis.RedisSortedSetService;
import pupket.togedogserver.global.s3.util.S3FileUtil;
import pupket.togedogserver.global.security.CustomUserDetail;

@ExtendWith(MockitoExtension.class)
class DogServiceImplTest {
    @Mock
    private DogRepository dogRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DogMapper dogMapper;
    @Mock
    private DogPersonalityTagRepository dogPersonalityTagRepository;
    @Mock
    private OwnerRepository ownerRepository;
    @Mock
    private S3FileUtil s3FileUtil;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private CustomDogRepositoryImpl customDogRepository;
    @Mock
    private RedisSortedSetService redisSortedSetService;

    @InjectMocks
    private DogServiceImpl dogService;

    private CustomUserDetail userDetail;
    private User user;
    private Dog dog;
    private Owner owner;
    private MockMultipartFile mockFile;

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

        owner = Owner.builder()
            .ownerUuid(1L)
            .user(user)
            .build();

        dog = Dog.builder()
            .dogId(1L)
            .name("테스트견")
            .age(3)
            .user(user)
            .dogGender(true)
            .dogType(DogType.MID)
            .breed("포메라니안")
            .weight(10L)
            .neutered(true)
            .region(Region.SEOUL)
            .notes("테스트 메모")
            .dogImage("test-image-url")
            .build();

        mockFile = new MockMultipartFile(
            "profileImage",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("강아지 등록 성공")
    void create_Success() {
        // Given
        DogRegistRequest request = createDogRegistRequest();
        when(userRepository.findByUuid(anyLong())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.getRefreshTokenByMemberId(anyLong()))
            .thenReturn(Optional.of(RefreshToken.builder()
                .refreshToken("test-token")
                .memberId(1L)
                .build()));
        when(ownerRepository.findByUser(any())).thenReturn(Optional.of(owner));
        when(dogMapper.toDog(any(), any())).thenReturn(dog);
        when(s3FileUtil.upload(any())).thenReturn("test-image-url");
        when(dogRepository.save(any())).thenReturn(dog);
        when(dogMapper.toDogPersonalityTags(any(), any())).thenReturn(Set.of(
            DogPersonalityTag.builder()
                .tag("활발한")
                .dog(dog)
                .build()
        ));

        // When
        dogService.create(userDetail, request, mockFile);

        // Then
        verify(dogRepository, times(2)).save(any(Dog.class));
    }

    @Test
    @DisplayName("강아지 조회 성공")
    void find_Success() {
        // Given
        DogResponse expectedResponse = createMockDogResponse();
        when(userRepository.findByUuid(anyLong())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.getRefreshTokenByMemberId(anyLong()))
            .thenReturn(Optional.of(RefreshToken.builder()
                .refreshToken("test-token")
                .memberId(1L)
                .build()));
        when(dogRepository.findById(anyLong())).thenReturn(Optional.of(dog));
        when(dogMapper.toResponse(any())).thenReturn(expectedResponse);

        // When
        DogResponse response = dogService.find(userDetail, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("테스트견");
    }

    @Test
    @DisplayName("강아지 수정 성공")
    void update_Success() {
        // Given
        DogUpdateRequest request = createDogUpdateRequest();
        when(userRepository.findByUuid(anyLong())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.getRefreshTokenByMemberId(anyLong()))
            .thenReturn(Optional.of(RefreshToken.builder()
                .refreshToken("test-token")
                .memberId(1L)
                .build()));
        when(dogRepository.findById(anyLong())).thenReturn(Optional.of(dog));
        when(s3FileUtil.upload(any())).thenReturn("new-image-url");
        doNothing().when(s3FileUtil).deleteImageFromS3(anyString());

        // When
        dogService.update(userDetail, request, mockFile);

        // Then
        verify(dogRepository).save(any(Dog.class));
    }

    @Test
    @DisplayName("강아지 삭제 성공")
    void delete_Success() {
        // Given
        when(userRepository.findByUuid(anyLong())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.getRefreshTokenByMemberId(anyLong()))
            .thenReturn(Optional.of(RefreshToken.builder()
                .refreshToken("test-token")
                .memberId(1L)
                .build()));
        when(dogRepository.findByUserAndDogId(any(), anyLong())).thenReturn(Optional.of(dog));

        // When
        dogService.delete(userDetail, 1L);

        // Then
        verify(dogRepository).deleteById(anyLong());
    }

    private DogRegistRequest createDogRegistRequest() {
        return DogRegistRequest.builder()
            .name("테스트견")
            .age(3)
            .dogGender(true)
            .breed("포메라니안")
            .weight(10)
            .neutered(true)
            .region(Region.SEOUL)
            .notes("테스트 메모")
            .tags(Set.of("활발한", "친절한"))
            .build();
    }

    private DogUpdateRequest createDogUpdateRequest() {
        return DogUpdateRequest.builder()
            .id(1L)
            .name("수정된견")
            .age(4)
            .breed("말티즈")
            .weight(8)
            .neutered(true)
            .region("경기")
            .notes("수정된 메모")
            .tags(Set.of("온순한"))
            .build();
    }

    private DogResponse createMockDogResponse() {
        return DogResponse.builder()
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
}