package potatowoong.potatomallback.global.auth.oauth.data;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getId() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return String.valueOf(response.get("id"));
    }
}
