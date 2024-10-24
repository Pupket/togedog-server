package pupket.togedogserver.domain.user.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

public interface MateService {

     void create(CustomUserDetail userDetail, RegistMateRequest request, MultipartFile profileImage);

     FindMateResponse find(CustomUserDetail userDetail);

     Page<FindMateResponse> findRandom(Pageable pageable);

     void update(CustomUserDetail userDetail, UpdateMateRequest request, MultipartFile profileImages);

     void delete(CustomUserDetail userDetail);
}
