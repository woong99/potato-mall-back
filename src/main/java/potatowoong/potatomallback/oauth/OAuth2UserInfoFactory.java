package potatowoong.potatomallback.oauth;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import potatowoong.potatomallback.oauth.data.GoogleOAuth2UserInfo;
import potatowoong.potatomallback.oauth.data.KakaoOAuth2UserInfo;
import potatowoong.potatomallback.oauth.data.NaverOAuth2UserInfo;
import potatowoong.potatomallback.oauth.data.OAuth2UserInfo;
import potatowoong.potatomallback.oauth.enums.SocialType;

@Getter
@RequiredArgsConstructor
public class OAuth2UserInfoFactory {

    private final OAuth2UserInfo oAuth2UserInfo;

    public static OAuth2UserInfo of(SocialType socialType, Map<String, Object> attributes) {
        if (socialType == SocialType.KAKAO) {
            return new KakaoOAuth2UserInfo(attributes);
        } else if (socialType == SocialType.NAVER) {
            return new NaverOAuth2UserInfo(attributes);
        } else {
            return new GoogleOAuth2UserInfo(attributes);
        }
    }
}

