package pupket.togedogserver.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.UserRepositoryImpl;
import pupket.togedogserver.domain.user.repository.jpaRepository.UserJPARepository;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {
    @Mock
    private UserJPARepository userJPARepository;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .uuid(1L)
            .email("test@test.com")
            .nickname("tester")
            .role(RoleType.MEMBER_KAKAO)
            .birthday(101)
            .birthyear(1990)
            .build();
    }

    @Test
    @DisplayName("이메일로 사용자 찾기 성공")
    void findByEmail_Success() {
        // Given
        when(userJPARepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userRepository.findByEmail("test@test.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("닉네임으로 사용자 찾기 성공")
    void findByNickname_Success() {
        // Given
        when(userJPARepository.findByNickname(anyString())).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userRepository.findByNickname("tester");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("UUID로 사용자 찾기 성공")
    void findByUuid_Success() {
        // Given
        when(userJPARepository.findByUuid(anyLong())).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userRepository.findByUuid(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(1L);
    }

    @Test
    @DisplayName("FCM 토큰 업데이트 성공")
    void updateFcmToken_Success() {
        // When
        userRepository.updateFcmTokenByUuid("test-token", 1L);

        // Then
        verify(userJPARepository).updateFcmTokenByUuid("test-token", 1L);
    }

    @Test
    @DisplayName("모든 닉네임 조회 성공")
    void findAllNicknames_Success() {
        // Given
        List<String> nicknames = List.of("user1", "user2");
        when(userJPARepository.findAllByNickname()).thenReturn(nicknames);

        // When
        List<String> result = userRepository.findAllNicknames();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("user1", "user2");
    }
}