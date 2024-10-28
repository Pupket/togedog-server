package pupket.togedogserver.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisLoginService {

    private final RedisTemplate<String,Object> redisTemplateForToken;


    public void saveAccessToken(String accessToken, Long id) {
        System.out.println("수행됨");
        String key = "userId: " + id;
        System.out.println("id"+"="+id);

        redisTemplateForToken.opsForValue().set(key, accessToken);
    }

    public String getAccessToken(String userId) {
        return (String)redisTemplateForToken.opsForValue().get("userId: " + userId);
    }
}
