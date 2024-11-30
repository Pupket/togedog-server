package pupket.togedogserver.domain.dog.service.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;

public interface CustomDogRepository {

    Page<DogResponse> dogList(Pageable pageable);

}
