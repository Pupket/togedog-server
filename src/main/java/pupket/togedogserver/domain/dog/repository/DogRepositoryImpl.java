package pupket.togedogserver.domain.dog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.repository.jpaRepository.DogJPARepository;
import pupket.togedogserver.domain.dog.service.port.DogRepository;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DogRepositoryImpl implements DogRepository {
    private final DogJPARepository dogJPARepository;

    @Override
    public Optional<List<Dog>> findByUser(User findUser) {
        return dogJPARepository.findByUser(findUser);
    }

    @Override
    public List<Dog> findAllByUser(User user) {
        return dogJPARepository.findAllByUser(user);
    }

    @Override
    public Optional<Dog> findByUserAndDogId(User findUser, Long id) {
        return dogJPARepository.findByUserAndDogId(findUser, id);
    }

    @Override
    public List<String> findAllBreedData() {
        return dogJPARepository.findAllBreedData();
    }

    @Override
    public Dog save(Dog dog) {
        return dogJPARepository.save(dog);
    }

    @Override
    public void deleteById(Long id) {
         dogJPARepository.deleteById(id);
    }

    @Override
    public Optional<Dog> findById(Long id) {

        return dogJPARepository.findById(id);
    }
}
