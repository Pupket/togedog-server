package pupket.togedogserver.global.auth.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import pupket.togedogserver.domain.user.constant.RoleType;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private String loginId;
    private RoleType role;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes, String nameAttributeKey,
                            String loginId, RoleType role) {
        super(authorities, attributes, nameAttributeKey);
        this.loginId = loginId;
        this.role = role;
    }
}
