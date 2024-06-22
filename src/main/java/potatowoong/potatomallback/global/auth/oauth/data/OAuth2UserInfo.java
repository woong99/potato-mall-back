package potatowoong.potatomallback.global.auth.oauth.data;

import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class OAuth2UserInfo {

    protected final Map<String, Object> attributes;

    public abstract String getId();
}
