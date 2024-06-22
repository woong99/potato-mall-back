package potatowoong.potatomallback.oauth.component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import potatowoong.potatomallback.jwt.component.JwtTokenProvider;
import potatowoong.potatomallback.jwt.dto.TokenDto;

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

        // Access Token의 만료 시간
        long refreshTokenExpiredIn = Duration.between(LocalDateTime.now(), tokenDto.refreshTokenDto().tokenExpiresIn()).getSeconds();

        // Refresh Token을 쿠키에 담아서 전달
        Cookie cookie = new Cookie("refreshToken", tokenDto.refreshTokenDto().token());
        cookie.setPath("/");
        cookie.setMaxAge((int) refreshTokenExpiredIn);
        cookie.setSecure(!activeProfile.equals("dev"));
        response.addCookie(cookie);

        // Refresh Token을 Redis에 저장
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(tokenDto.refreshTokenDto().token(), authentication.getName(), refreshTokenExpiredIn);

        response.sendRedirect(redirectUrl + "/oauth2-login-success?accessToken=" + tokenDto.accessTokenDto().token());
    }
}
