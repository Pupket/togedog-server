package pupket.togedogserver.domain.dog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.user.entity.User;
import java.util.List;
import java.util.Optional;

public interface DogRepository extends JpaRepository<Dog, Long> {

    Optional<List<Dog>> findByUser(User findUser);

    Optional<Dog> findByUserAndName(User user, String name);
}
