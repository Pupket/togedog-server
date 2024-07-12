package pupket.togedogserver.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.user.entity.mate.MateTag;

public interface MateTagRepository extends JpaRepository<MateTag, Long> {
}
