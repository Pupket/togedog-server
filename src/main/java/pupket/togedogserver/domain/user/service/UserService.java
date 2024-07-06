package pupket.togedogserver.domain.user.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindUserResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

public interface UserService {

    public ResponseEntity<Void> signUp(@Validated @RequestBody RegistMateRequest request);

    public ResponseEntity<FindUserResponse> find(@AuthenticationPrincipal CustomUserDetail user);

    public ResponseEntity<Void> update(@AuthenticationPrincipal CustomUserDetail user, @RequestBody UpdateMateRequest updateReq);

    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response);

    public ResponseEntity<String> reissue(HttpServletRequest request, HttpServletResponse response);

}