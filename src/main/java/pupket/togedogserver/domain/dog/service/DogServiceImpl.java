package pupket.togedogserver.domain.dog.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.dog.controller.port.DogService;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.entity.DogPersonalityTag;
import pupket.togedogserver.domain.dog.mapper.DogMapper;
import pupket.togedogserver.domain.dog.repository.CustomDogRepositoryImpl;
import pupket.togedogserver.domain.dog.service.port.DogPersonalityTagRepository;
import pupket.togedogserver.domain.dog.service.port.DogRepository;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.service.port.OwnerRepository;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.DogException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.redis.RedisSortedSetService;
import pupket.togedogserver.global.s3.util.S3FileUtil;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.trie.Trie;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DogServiceImpl implements DogService {

    private final DogRepository dogRepository;
    private final UserRepository userRepository;
    private final DogMapper dogMapper;
    private final DogPersonalityTagRepository dogPersonalityTagRepository;
    private final OwnerRepository ownerRepository;
    private final S3FileUtil s3FileUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomDogRepositoryImpl customDogRepository;
    private final RedisSortedSetService redisSortedSetService;
    private final Trie trie = new Trie(); // Trie 인스턴스 생성

    @PostConstruct
    public void init() {

        List<String> dogBreedList = dogRepository.findAllBreedData();
        log.info("data Size={}", dogBreedList.size());

        for (String breed : dogBreedList) {
            trie.insert(breed); // Trie에 모든 단어 삽입
        }
    }


    @Override
    public void create(CustomUserDetail user, DogRegistRequest request, MultipartFile profileImages) {
        User findUser = getUserById(user.getUuid());

        // 5마리 이상 등록하면 예외 발생
        validationDogCount(findUser);

        Owner findOwner = ownerRepository.findByUser(findUser).orElse(null);
        if (findOwner == null) {
            saveOwner(findUser);
        }

        Dog createdDog = dogMapper.toDog(request, findUser);
        dogRepository.save(createdDog);

        Set<DogPersonalityTag> tags = dogMapper.toDogPersonalityTags(request.getTags(), createdDog);
        String uploadedDogImage = getUploadedDogImage(profileImages);

        createdDog = createdDog.toBuilder()
                .dogPersonalityTags(tags)
                .dogImage(uploadedDogImage)
                .build();

        dogRepository.save(createdDog);
        dogPersonalityTagRepository.saveAll(tags);

        log.info("Dog created with ID: {}", createdDog.getDogId());
    }

    private void validationDogCount(User findUser) {
        if (dogRepository.findAllByUser(findUser).size() + 1 >= 6) {
            log.warn("User {} has exceeded the dog registration limit", findUser.getUuid());
            throw new DogException(ExceptionCode.AVAILABLE_FOR_REGISTRATION_EXCEEDED);
        }
    }

    private String getUploadedDogImage(MultipartFile profileImages) {
        if (profileImages != null) {
            return s3FileUtil.upload(profileImages);
        }
        return null;
    }

    private void saveOwner(User findUser) {
        Owner owner = Owner.builder().user(findUser).build();
        User updatedUser = findUser.toBuilder().owner(owner).build();

        ownerRepository.save(owner);
        userRepository.save(updatedUser);
    }

    @Override
    public void update(CustomUserDetail user, DogUpdateRequest request, MultipartFile profileImage) {
        getUserById(user.getUuid());
        Dog findDog = findDogById(dogRepository.findById(request.getId()));

        DogType dogType = determineDogTypeBasedOnWeight(request);

        if (findDog.getDogImage() != null) {
            s3FileUtil.deleteImageFromS3(findDog.getDogImage());
        }

        String uploadedDogImage = getUploadedDogImage(profileImage);

        findDog = findDog.toBuilder()
                .name(request.getName())
                .dogType(dogType)
                .breed(request.getBreed())
                .name(request.getName())
                .neutered(request.isNeutered())
                .weight((long) request.getWeight())
                .region(Region.nameOf(request.getRegion()))
                .notes(request.getNotes())
                .age(request.getAge())
                .dogImage(uploadedDogImage)
                .build();

        dogPersonalityTagRepository.deleteAllByDog(findDog);
        Set<DogPersonalityTag> tags = dogMapper.toDogPersonalityTags(request.getTags(), findDog);

        findDog.toBuilder().dogPersonalityTags(tags).build();
        dogRepository.save(findDog);
        dogPersonalityTagRepository.saveAll(tags);

        log.info("Dog updated with ID: {}", findDog.getDogId());
    }

    private static DogType determineDogTypeBasedOnWeight(DogUpdateRequest request) {
        int weight = request.getWeight();
        DogType dogType;
        if (weight >= 40) {
            dogType = DogType.SUPER;
        } else if (weight >= 16) {
            dogType = DogType.BIG;
        } else if (weight > 7) {
            dogType = DogType.MID;
        } else {
            dogType = DogType.SMALL;
        }
        return dogType;
    }

    @Override
    public void delete(CustomUserDetail user, Long id) {
        User findUser = getUserById(user.getUuid());

        Dog findDog = findDogById(dogRepository.findByUserAndDogId(findUser, id));

        if (findDog.getDogImage() != null) {
            s3FileUtil.deleteImageFromS3(findDog.getDogImage());
        }

        dogRepository.deleteById(id);
        dogPersonalityTagRepository.deleteByDog(findDog);
    }

    @Override
    public DogResponse find(CustomUserDetail user, Long id) {
        getUserById(user.getUuid());

        Dog findDog = findDogById(dogRepository.findById(id));

        DogResponse dogResponse = dogMapper.toResponse(findDog);

        dogMapper.afterMapping(dogResponse, findDog);

        return dogResponse;
    }

    private Dog findDogById(Optional<Dog> dogRepository) {
        return dogRepository.orElseThrow(() -> {
            log.error("Dog not found");
            return new DogException(ExceptionCode.NOT_FOUND_DOG);
        });
    }

    @Override
    public List<DogResponse> findAll(CustomUserDetail user) {
        User findUser = getUserById(user.getUuid());

        List<Dog> dogList = getDogList(findUser);

        return dogList.stream()
                .map(dog -> {
                    DogResponse dogResponse = dogMapper.toResponse(dog);
                    dogMapper.afterMapping(dogResponse, dog);
                    return dogResponse;
                })
                .toList();

    }

    private List<Dog> getDogList(User findUser) {
        return dogRepository.findByUser(findUser).orElseThrow(() ->
                new DogException(ExceptionCode.NOT_FOUND_DOG)
        );
    }

    private User getUserById(Long uuid) {
        refreshTokenRepository.getRefreshTokenByMemberId(uuid).orElseThrow(() -> {
            log.error("Refresh token not found for user: {}", uuid);
            return new MemberException(ExceptionCode.NOT_FOUND_REFRESH_TOKEN);
        });
        return userRepository.findByUuid(uuid).orElseThrow(() -> {
            log.error("User not found with UUID: {}", uuid);
            return new MemberException(ExceptionCode.NOT_FOUND_MEMBER);
        });
    }

    @Override
    public Page<DogResponse> findRandom(Pageable pageable) {
        return customDogRepository.dogList(pageable);
    }

    @Override
    public List<String> autoCompleteKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            log.info("검색어가 비어 있음");
            return Collections.emptyList();
        }

        // Trie를 사용해 접두사에 해당하는 단어 검색
        List<String> results = trie.searchByPrefix(keyword, 10);

        if (results.isEmpty()) {
            log.info("자동완성 결과 없음");
        }
        results.forEach(
                data ->
                log.info("result={}",data)
        );

        return results;
    }
}
