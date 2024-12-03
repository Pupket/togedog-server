package pupket.togedogserver.global.jwt.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Data
@AllArgsConstructor
@Getter
public class JwtToken {

    private String grantType;
    private String accessToken;
    private String refreshToken;
}
