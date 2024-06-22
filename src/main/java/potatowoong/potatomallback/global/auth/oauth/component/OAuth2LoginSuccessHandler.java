package potatowoong.potatomallback.global.auth.oauth.component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import potatowoong.potatomallback.global.auth.jwt.component.JwtTokenProvider;
import potatowoong.potatomallback.global.auth.jwt.dto.RefreshTokenDto;
import potatowoong.potatomallback.global.auth.jwt.dto.TokenDto;
import potatowoong.potatomallback.global.utils.CookieUtils;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    private final StringRedisTemplate redisTemplate;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${oauth2.redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);
        RefreshTokenDto refreshTokenDto = tokenDto.refreshTokenDto();

        // Refresh Token을 Redis에 저장
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(tokenDto.refreshTokenDto().token(), authentication.getName(), refreshTokenDto.expiresIn());

        // Refresh Token을 쿠키에 담아서 전달
        Cookie cookie = CookieUtils.createCookie("refreshToken", tokenDto.refreshTokenDto().token(), (int) refreshTokenDto.expiresIn(), activeProfile.equals("prod"));
        response.addCookie(cookie);

        response.sendRedirect(redirectUrl + "/oauth2-login-success?accessToken=" + tokenDto.accessTokenDto().token());
    }
}
