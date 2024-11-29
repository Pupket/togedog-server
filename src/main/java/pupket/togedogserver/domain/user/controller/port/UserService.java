package pupket.togedogserver.domain.user.controller.port;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateAndDogResponse;
import pupket.togedogserver.domain.user.dto.response.FindUserInfoResponse;
import pupket.togedogserver.domain.user.dto.response.FindUserResponse;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.security.CustomUserDetail;

@Service
public interface UserService {

    void create(CustomUserDetail userDetail, RegistMateRequest request);

    public void logout(String refreshToken, CustomUserDetail userDetail);

    public JwtToken reissueToken(String refreshToken);

    public FindUserInfoResponse getMemberDetails(Long uuid);

    public void deleteSocialMember(Long uuid);

    public String getRefreshToken(Long uuid);

    public FindMateAndDogResponse findMateAndDogActive(CustomUserDetail userDetail);

    public JwtToken reissueTokenWithValidation(String refreshTokenInRequest, String accessToken);

}