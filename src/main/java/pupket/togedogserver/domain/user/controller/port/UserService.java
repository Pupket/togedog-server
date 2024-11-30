package pupket.togedogserver.domain.user.controller.port;


import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateAndDogResponse;
import pupket.togedogserver.domain.user.dto.response.FindUserInfoResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.security.CustomUserDetail;

@Service
public interface UserService {

    User create(CustomUserDetail userDetail, RegistMateRequest request);

    public void logout(String refreshToken, CustomUserDetail userDetail);

    public JwtToken reissueToken(String refreshToken);

    public FindUserInfoResponse getMemberDetails(Long uuid);

    public void deleteSocialMember(Long uuid);

    public String getRefreshToken(Long uuid);

    public FindMateAndDogResponse findMateAndDogActive(CustomUserDetail userDetail);

    public JwtToken reissueTokenWithValidation(String refreshTokenInRequest, String accessToken);

}