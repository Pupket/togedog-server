package pupket.togedogserver.domain.dog.service;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;

public interface DogService {
     void create(@AuthenticationPrincipal CustomUserDetail user, DogRegistRequest request, MultipartFile profileImages);

     void update(@AuthenticationPrincipal CustomUserDetail user, DogUpdateRequest request, MultipartFile profileImages);

     void delete(@AuthenticationPrincipal CustomUserDetail user, @PathVariable Long id);

     DogResponse find(@AuthenticationPrincipal CustomUserDetail user, @PathVariable Long id);

     List<DogResponse> findAll(@AuthenticationPrincipal CustomUserDetail user);
}
