package pupket.togedogserver.domain.user.repository.mateRepo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;

public interface CustomMateRepository {
    Page<FindMateResponse> MateList(Pageable pageable);

}
