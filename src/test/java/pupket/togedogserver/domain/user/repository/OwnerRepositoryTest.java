package pupket.togedogserver.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.OwnerRepositoryImpl;
import pupket.togedogserver.domain.user.repository.jpaRepository.OwnerJPARepository;

@ExtendWith(MockitoExtension.class)
class OwnerRepositoryTest {
    @Mock
    private OwnerJPARepository ownerJPARepository;

    @InjectMocks
    private OwnerRepositoryImpl ownerRepository;

    private User user;
    private Owner owner;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .uuid(1L)
            .email("test@test.com")
            .nickname("tester")
            .build();

        owner = Owner.builder()
            .ownerUuid(1L)
            .user(user)
            .build();
    }

    @Test
    @DisplayName("사용자로 견주 찾기 성공")
    void findByUser_Success() {
        // Given
        when(ownerJPARepository.findByUser(any(User.class))).thenReturn(Optional.of(owner));

        // When
        Optional<Owner> result = ownerRepository.findByUser(user);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("견주 저장 성공")
    void save_Success() {
        // Given
        when(ownerJPARepository.save(any(Owner.class))).thenReturn(owner);

        // When
        Owner result = ownerRepository.save(owner);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOwnerUuid()).isEqualTo(1L);
    }
}