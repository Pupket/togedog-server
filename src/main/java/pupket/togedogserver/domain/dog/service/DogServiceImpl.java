package pupket.togedogserver.domain.dog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.mapper.DogMapper;
import pupket.togedogserver.domain.dog.repository.DogRepository;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.DogException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.security.CustomUserDetail;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DogServiceImpl implements DogService {

    private final DogRepository dogRepository;
    private final UserRepository userRepository;
    private final DogMapper dogMapper;


    @Override
    public void create(CustomUserDetail user, DogRegistRequest request) {
        User findUser = getUserById(user.getUuid());
        dogRepository.findByUserAndName(findUser, request.getName()).ifPresent(dog -> {
                    throw new DogException(ExceptionCode.DOG_ALREADY_EXISTS);
                }
        );

        Dog mappedDog = dogMapper.toDog(request);

        Dog dog = mappedDog.toBuilder()
                .user(findUser)
                .build();

        dogRepository.save(dog);
    }

    @Override
    public void update(CustomUserDetail user, DogUpdateRequest request) {
        getUserById(user.getUuid());

        Dog findDog = dogRepository.findById(request.getId()).orElseThrow(() ->
                new DogException(ExceptionCode.NOT_FOUND_DOG)
        );

        dogMapper.updateDogFromRequest(request, findDog);

        dogRepository.save(findDog);
    }


    @Override
    public void delete(CustomUserDetail user, Long id) {
        getUserById(user.getUuid());

        dogRepository.deleteById(id);
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
