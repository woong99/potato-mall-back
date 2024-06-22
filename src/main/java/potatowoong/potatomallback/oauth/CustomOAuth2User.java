package potatowoong.potatomallback.oauth;

import java.util.Collections;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import potatowoong.potatomallback.auth.enums.Role;

@Getter
@EqualsAndHashCode(callSuper = false)
public class CustomOAuth2User extends DefaultOAuth2User {

    private final String userId;

    private final Role role;

    public CustomOAuth2User(Map<String, Object> attributes, String nameAttributeKey, String userId) {
        super(Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_USER.name())), attributes, nameAttributeKey);
        this.userId = userId;
        this.role = Role.ROLE_USER;
    }
}
