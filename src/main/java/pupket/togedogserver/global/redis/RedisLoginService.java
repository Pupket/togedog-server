package pupket.togedogserver.global.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;

import java.time.Duration;
import java.util.Objects;

@Service
@Slf4j
public class RedisLoginService {

    RedisLoginService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TOKEN_EXPIRATION = Duration.ofHours(2); // 토큰 유효 시간

    public void storeUserIPAddressInRedis(User user, String ipAddress) {
        String key = "user:" + user.getUuid();
        redisTemplate.opsForValue().set(key, ipAddress, TOKEN_EXPIRATION);
        log.info("Stored IP {} for user {}", ipAddress, user.getUuid());

    }

    public void validateDuplicateLogin(User user, String ipAddress) {
        String key = "user:" + user.getUuid();
        String storedAccessToken = redisTemplate.opsForValue().get(key);

        // IP가 "IPX"일 경우, 검증을 건너뜁니다.
        if(ipAddress.equals("IPX")) {
            return;
        }

        // 기존 IP와 요청한 IP가 다를 경우
        if (storedAccessToken != null && !storedAccessToken.equals(ipAddress)) {
            // 기존 정보를 삭제
            redisTemplate.delete(key);
            // 새로운 IP를 사용자 정보로 저장
            storeUserIPAddressInRedis(user, ipAddress);
            log.info("Stored new IP {} for user {} after removing old IP {}", ipAddress, user.getUuid(), storedAccessToken);

            // 중복 로그인 예외를 던짐
            throw new MemberException(ExceptionCode.DUPLICATE_LOGIN);
        }
    }
}
