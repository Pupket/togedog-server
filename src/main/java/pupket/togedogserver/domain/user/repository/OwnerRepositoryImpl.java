package pupket.togedogserver.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.jpaRepository.OwnerJPARepository;
import pupket.togedogserver.domain.user.service.port.OwnerRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OwnerRepositoryImpl implements OwnerRepository {
    private final OwnerJPARepository ownerJPARepository;
    @Override
    public Optional<Owner> findByUser(User user) {
        return ownerJPARepository.findByUser(user);
    }

    @Override
    public Owner save(Owner owner) {
        return ownerJPARepository.save(owner);
    }

}
