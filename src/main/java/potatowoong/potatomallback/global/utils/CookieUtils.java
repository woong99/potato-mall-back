package potatowoong.potatomallback.global.utils;

import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtils {

    /**
     * 쿠키 생성
     *
     * @param cookieName   쿠키명
     * @param value        쿠키값
     * @param maxAge       쿠키 만료 시간
     * @param isProduction 운영 환경 여부
     * @return 생성된 쿠키
     */
    public static Cookie createCookie(String cookieName, String value, int maxAge, boolean isProduction) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setHttpOnly(isProduction);
        cookie.setSecure(isProduction);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");

        return cookie;
    }

}
