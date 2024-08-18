package pupket.togedogserver.domain.user.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

public interface MateService {

    public void create(CustomUserDetail userDetail, RegistMateRequest request, MultipartFile profileImage);

    public FindMateResponse find(CustomUserDetail userDetail);

    public Page<FindMateResponse> findRandom(Pageable pageable);

    public void update(CustomUserDetail userDetail, UpdateMateRequest request, MultipartFile profileImages);

    public void delete(CustomUserDetail userDetail);
}
