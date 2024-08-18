package pupket.togedogserver.domain.dog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.dog.constant.Breed;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.entity.DogPersonalityTag;
import pupket.togedogserver.domain.dog.mapper.DogMapper;
import pupket.togedogserver.domain.dog.repository.DogPersonalityTagRepository;
import pupket.togedogserver.domain.dog.repository.DogRepository;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.OwnerRepository;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.DogException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.s3.util.S3FileUtilImpl;
import pupket.togedogserver.global.security.CustomUserDetail;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private final S3FileUtilImpl s3FileUtilImpl;


    @Override
    public void create(CustomUserDetail user, DogRegistRequest request, MultipartFile profileImages) {
        User findUser = getUserById(user.getUuid());

        if (dogRepository.findAllByUser(findUser).size() + 1 >= 6) {
            throw new DogException(ExceptionCode.AVAILABLE_FOR_REGISTRATION_EXCEEDED);
        }

        dogRepository.findByUserAndName(findUser, request.getName()).ifPresent(dog -> {
                    throw new DogException(ExceptionCode.DOG_ALREADY_EXISTS);
                }
        );
        saveOwner(findUser);

        Dog createdDog = dogMapper.toDog(request, findUser);

        dogRepository.save(createdDog);

        Set<DogPersonalityTag> tags = dogMapper.toDogPersonalityTags(request.getTags(), createdDog);

        String uploadedDogImage = s3FileUtilImpl.upload(profileImages);

        createdDog = createdDog.toBuilder()
                .dogPersonalityTags(tags)
                .dogImage(uploadedDogImage)
                .build();

        dogRepository.save(createdDog);


        tags.forEach(dogPersonalityTagRepository::save);

    }

    private void saveOwner(User findUser) {
        Owner owner = Owner.builder()
                .user(findUser)
                .build();

        User updatedUser = findUser.toBuilder()
                .owner(owner)
                .build();


        ownerRepository.save(owner);
        userRepository.save(updatedUser);
    }

    @Override
    public void update(CustomUserDetail user, DogUpdateRequest request, MultipartFile profileImage) {
        getUserById(user.getUuid());

        Dog findDog = dogRepository.findById(request.getId()).orElseThrow(() ->
                new DogException(ExceptionCode.NOT_FOUND_DOG)
        );

        Breed breed = getBreed(request);

        s3FileUtilImpl.deleteImageFromS3(findDog.getDogImage());
        String uploadedDogImage = s3FileUtilImpl.upload(profileImage);

        findDog = findDog.toBuilder()
                .breed(breed)
                .name(request.getName())
                .neutered(request.isNeutered())
                .weight((long) request.getWeight())
                .region(request.getRegion())
                .notes(request.getNotes())
                .age(request.getAge())
                .dogImage(uploadedDogImage)
                .build();

        findDog = dogRepository.save(findDog);

        dogPersonalityTagRepository.deleteAllByDog(findDog);

        Set<DogPersonalityTag> tags = dogMapper.toDogPersonalityTags(request.getTag(), findDog);

        findDog = findDog.toBuilder()
                .dogPersonalityTags(tags)
                .build();

        dogRepository.save(findDog);

        tags.forEach(dogPersonalityTagRepository::save);
    }

    private static Breed getBreed(DogUpdateRequest request) {
        int weight = request.getWeight();
        Breed breed = null;
        if (weight >= 40) {
            breed = Breed.SUPER;
        } else if (weight >= 16 && weight < 40) {
            breed = Breed.BIG;
        } else if (weight > 7 && weight <= 15) {
            breed = Breed.MID;
        } else {
            breed = Breed.SMALL;
        }
        return breed;
    }

    @Override
    public void delete(CustomUserDetail user, Long id) {
        User findUser = getUserById(user.getUuid());

        Dog findDog = dogRepository.findByUserAndDogId(findUser, id).orElseThrow(
                () -> new DogException(ExceptionCode.NOT_FOUND_DOG)
        );

        s3FileUtilImpl.deleteImageFromS3(findDog.getDogImage());

        dogRepository.deleteById(id);
        dogPersonalityTagRepository.deleteByDog(findDog);
    }

    @Override
    public DogResponse find(CustomUserDetail user, Long id) {
        getUserById(user.getUuid());

        Dog findDog = dogRepository.findById(id).orElseThrow(() ->
                new DogException(ExceptionCode.NOT_FOUND_DOG)
        );

        DogResponse dogResponse = dogMapper.toResponse(findDog);

        return dogResponse;
    }

    @Override
    public List<DogResponse> findAll(CustomUserDetail user) {
        User findUser = getUserById(user.getUuid());

        List<Dog> dogList = dogRepository.findByUser(findUser).orElseThrow(() ->
                new DogException(ExceptionCode.NOT_FOUND_DOG)
        );

        List<DogResponse> dogResponseList = new ArrayList<>();
        dogList.forEach(dog -> dogResponseList.add(dogMapper.toResponse(dog)));

        return dogResponseList;

    }

    private User getUserById(Long memberUuid) {
        return userRepository.findByUuid(memberUuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
    }
}
