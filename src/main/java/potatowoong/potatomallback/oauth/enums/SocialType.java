package potatowoong.potatomallback.oauth.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SocialType {
    KAKAO("kakao"), NAVER("naver"), GOOGLE("google");

    private final String socialName;
}
