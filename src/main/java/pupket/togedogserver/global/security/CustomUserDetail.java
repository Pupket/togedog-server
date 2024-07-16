package pupket.togedogserver.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.global.security.util.PasswordUtil;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Getter
public class CustomUserDetail extends org.springframework.security.core.userdetails.User implements OAuth2User {

    private final Long uuid; // 회원 id
    private Map<String, Object> attributes;

    public CustomUserDetail(String username, String password, Long id, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.uuid = id;
    }

    public CustomUserDetail(User user, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this(user.getEmail() != null ? user.getEmail() : UUID.randomUUID().toString().substring(0, 8) + "@social.com",
                user.getPassword() != null ? user.getPassword() : PasswordUtil.generateRandomPassword(),
                user.getUuid(), authorities);
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}