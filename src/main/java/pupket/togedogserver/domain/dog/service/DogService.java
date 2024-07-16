package pupket.togedogserver.domain.dog.service;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;

public interface DogService {
    public void create(@AuthenticationPrincipal CustomUserDetail user, DogRegistRequest request);

    public void update(@AuthenticationPrincipal CustomUserDetail user, DogUpdateRequest request);

    public void delete(@AuthenticationPrincipal CustomUserDetail user, @PathVariable Long id);

    public DogResponse find(@AuthenticationPrincipal CustomUserDetail user, @PathVariable Long id);

    public List<DogResponse> findAll(@AuthenticationPrincipal CustomUserDetail user);
}
