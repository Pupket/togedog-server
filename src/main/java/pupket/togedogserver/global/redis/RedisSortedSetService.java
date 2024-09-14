package pupket.togedogserver.global.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class RedisSortedSetService {    //검색어 자동 완성을 구현할 때 사용하는 Redis의 SortedSet 관련 서비스 레이어

    RedisSortedSetService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private final RedisTemplate<String, String> redisTemplate;
    private String keyUsedByDog = "breedName"; //검색어 자동 완성을 위한 Redis 데이터
    private String keyUsedByMate = "userNickname"; //검색어 자동 완성을 위한 Redis 데이터
    private int score = 0;  //Score는 딱히 필요 없으므로 하나로 통일

    public void addToSortedSetFromMate(String value) {    //Redis SortedSet에 추가
        redisTemplate.opsForZSet().add(keyUsedByMate, value, score);
    }

    public void addToSortedSetFromDog(String value) {    //Redis SortedSet에 추가
        redisTemplate.opsForZSet().add(keyUsedByDog, value, score);
    }

    public Long findFromSortedSetFromMate(String value) {
        return redisTemplate.opsForZSet().rank(keyUsedByMate,value);
    }

    public Long findFromSortedSetFromDog(String value) {
        return redisTemplate.opsForZSet().rank(keyUsedByDog,value);
    }

    public Set<String> findAllValuesInMateAfterIndexFromSortedSet(Long index) {
        return redisTemplate.opsForZSet().range(keyUsedByMate, index, index + 2000);
    }
    public Set<String> findAllValuesInDogAfterIndexFromSortedSet(Long index) {
        return redisTemplate.opsForZSet().range(keyUsedByDog, index, index + 2000);
    }

    // **닉네임 삭제 로직 추가**
    public void removeFromSortedSetFromMate(String value) {
        redisTemplate.opsForZSet().remove(keyUsedByMate, value);
    }

    public void removeFromSortedSetFromDog(String value) {
        redisTemplate.opsForZSet().remove(keyUsedByDog, value);
    }


}