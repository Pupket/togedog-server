package pupket.togedogserver.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.repository.jpaRepository.MateJPARepository;
import pupket.togedogserver.domain.user.service.port.MateRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MateRepositoryImpl implements MateRepository {
    private final MateJPARepository mateRepository;


    @Override
    public Optional<Mate> findByUser(User User) {
        return mateRepository.findByUser(User);
    }

    @Override
    public Mate save(Mate Mate) {
        return mateRepository.save(Mate);
    }

    @Override
    public void delete(Mate findMateId) {
        mateRepository.delete(findMateId);
    }
}
