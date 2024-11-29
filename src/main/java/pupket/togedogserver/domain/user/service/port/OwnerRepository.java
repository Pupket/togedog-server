package pupket.togedogserver.domain.user.service.port;

import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;

import java.util.Optional;

@Repository
public interface OwnerRepository {
    Optional<Owner> findByUser(User user);

    Owner save(Owner owner);
}
