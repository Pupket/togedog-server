package pupket.togedogserver.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
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

import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredBreed;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredTime;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredWeek;
import pupket.togedogserver.domain.user.entity.mate.MateTag;
import pupket.togedogserver.domain.user.repository.*;
import pupket.togedogserver.domain.user.repository.jpaRepository.*;

@ExtendWith(MockitoExtension.class)
class MateRepositoryTest {
    @Mock
    private MateJPARepository mateJPARepository;
    @Mock
    private MateTagJPARepository mateTagJPARepository;
    @Mock
    private MatePreferredBreedJPARepository matePreferredBreedJPARepository;
    @Mock
    private MatePreferredTimeJPARepository matePreferredTimeJPARepository;
    @Mock
    private MatePreferredWeekJPARepository matePreferredWeekJPARepository;

    @InjectMocks
    private MateRepositoryImpl mateRepository;
    @InjectMocks
    private MateTagRepositoryImpl mateTagRepository;
    @InjectMocks
    private MatePreferredBreedRepositoryImpl matePreferredBreedRepository;
    @InjectMocks
    private MatePreferredTimeRepositoryImpl matePreferredTimeRepository;
    @InjectMocks
    private MatePreferredWeekRepositoryImpl matePreferredWeekRepository;

    private User user;
    private Mate mate;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .uuid(1L)
            .email("test@test.com")
            .nickname("tester")
            .build();

        mate = Mate.builder()
            .mateUuid(1L)
            .user(user)
            .matchCount(0L)
            .career("테스트 경력")
            .accommodatableDogsCount(2)
            .preferredRegion(Region.SEOUL)
            .preferredBreeds(new HashSet<>())
            .preferredTimes(new HashSet<>())
            .preferredWeeks(new HashSet<>())
            .mateTags(new HashSet<>())
            .build();
    }

    @Test
    @DisplayName("메이트 저장 성공")
    void save_Success() {
        // Given
        when(mateJPARepository.save(any(Mate.class))).thenReturn(mate);

        // When
        Mate result = mateRepository.save(mate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMateUuid()).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자로 메이트 찾기 성공")
    void findByUser_Success() {
        // Given
        when(mateJPARepository.findByUser(any(User.class))).thenReturn(Optional.of(mate));

        // When
        Optional<Mate> result = mateRepository.findByUser(user);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("메이트 태그 저장 성공")
    void saveTag_Success() {
        // Given
        MateTag mateTag = MateTag.builder()
            .tagName("친절한")
            .mate(mate)
            .build();
        when(mateTagJPARepository.save(any(MateTag.class))).thenReturn(mateTag);

        // When
        MateTag result = mateTagRepository.save(mateTag);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTagName()).isEqualTo("친절한");
    }

    @Test
    @DisplayName("메이트 선호 시간대 저장 성공")
    void savePreferredTime_Success() {
        // Given
        Set<MatePreferredTime> times = new HashSet<>();
        when(matePreferredTimeJPARepository.saveAll(any())).thenReturn(List.of());

        // When
        matePreferredTimeRepository.saveAll(times);

        // Then
        verify(matePreferredTimeJPARepository).saveAll(times);
    }
}