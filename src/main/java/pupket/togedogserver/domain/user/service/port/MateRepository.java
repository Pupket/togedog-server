package pupket.togedogserver.domain.user.service.port;

import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.util.Optional;

public interface MateRepository {
    Optional<Mate> findByUser(User User);

    Mate save(Mate Mate);

    void delete(Mate mate);
}
