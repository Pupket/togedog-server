package pupket.togedogserver.global.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.global.exception.customException.MemberException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class RedisLoginServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisLoginService redisLoginService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
        // RedisTemplate의 opsForValue() 메서드가 valueOperations를 반환하도록 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }


    @Test
    public void testValidateDuplicateLogin_withDifferentIp_shouldThrowException() {

        User testUser = User.builder()
                .uuid(100L)
                .build();
        // Given
        String storedIpAddress = "192.168.1.1";
        String newIpAddress = "192.168.1.2";
        String key = "user:" + testUser.getUuid();

        // When
        when(redisTemplate.opsForValue().get(key)).thenReturn(storedIpAddress);

        // Then
        assertThrows(MemberException.class, () -> redisLoginService.validateDuplicateLogin(testUser, newIpAddress));
    }

    @Test
    public void testValidateDuplicateLogin_withSameIp_shouldPass() {

        User testUser = User.builder()
                .uuid(100L)
                .build();
        // Given
        String storedIpAddress = "192.168.1.1";
        String newIpAddress = "192.168.1.1";
        String key = "user:" + testUser.getUuid();

        // When
        when(redisTemplate.opsForValue().get(key)).thenReturn(storedIpAddress);

        // Then
        redisLoginService.validateDuplicateLogin(testUser, newIpAddress);
    }

    @Test
    public void testValidateDuplicateLogin_withNoStoredIp_shouldPass() {


        User testUser = User.builder()
                .uuid(100L)
                .build();
        // Given
        String newIpAddress = "192.168.1.1";
        String key = "user:" + testUser.getUuid();

        // When
        when(redisTemplate.opsForValue().get(key)).thenReturn(null);

        // Then
        redisLoginService.validateDuplicateLogin(testUser, newIpAddress);
    }
}
