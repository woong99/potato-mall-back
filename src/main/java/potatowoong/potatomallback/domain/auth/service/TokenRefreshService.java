package potatowoong.potatomallback.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import potatowoong.potatomallback.domain.auth.enums.Role;
import potatowoong.potatomallback.domain.auth.enums.TokenName;
import potatowoong.potatomallback.global.auth.jwt.component.JwtTokenProvider;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.global.utils.CookieUtils;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final StringRedisTemplate redisTemplate;

    private final JwtTokenProvider jwtTokenProvider;

    public AccessTokenDto adminRefresh(HttpServletRequest request) {
        final String refreshToken = CookieUtils.getCookieValue(request.getCookies(), TokenName.ADMIN_REFRESH_TOKEN.name());
        return refresh(refreshToken, Role.ROLE_ADMIN);
    }

    public AccessTokenDto userRefresh(HttpServletRequest request) {
        final String refreshToken = CookieUtils.getCookieValue(request.getCookies(), TokenName.USER_REFRESH_TOKEN.name());
        return refresh(refreshToken, Role.ROLE_USER);
    }

    /**
     * Refresh Token으로 Access Token 갱신
     */
    private AccessTokenDto refresh(final String refreshToken, Role role) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // Redis에서 Refresh Token으로 ID 가져오기
        String userName = redisTemplate.opsForValue().get(refreshToken);
        if (StringUtils.isBlank(userName)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(userName, "", List.of(role::name));

        // 인증 정보를 기반으로 JWT Token 생성
        return jwtTokenProvider.generateAccessToken(authentication);
    }
}
