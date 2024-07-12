package pupket.togedogserver.domain.user.service;


import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

public interface MateService {

    public void create(CustomUserDetail userDetail, RegistMateRequest request);

    public FindMateResponse find(CustomUserDetail userDetail);

    public void update(CustomUserDetail userDetail, UpdateMateRequest request);

    public void delete(CustomUserDetail userDetail);
}
