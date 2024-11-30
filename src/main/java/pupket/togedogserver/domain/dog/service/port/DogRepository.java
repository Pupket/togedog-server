package pupket.togedogserver.domain.dog.service.port;


import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.user.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DogRepository {
    Optional<List<Dog>> findByUser(User findUser);

    Collection<Dog> findAllByUser(User user);

    Optional<Dog> findByUserAndDogId(User findUser, Long id);

    List<String> findAllBreedData();

    Dog save(Dog dog);

    void deleteById(Long id);

    Optional<Dog> findById(Long id);
}
