package potatowoong.potatomallback.domain.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.domain.auth.entity.Admin;
import potatowoong.potatomallback.domain.auth.enums.Role;
import potatowoong.potatomallback.domain.auth.repository.AdminRepository;
import potatowoong.potatomallback.global.auth.jwt.component.JwtTokenProvider;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.auth.jwt.dto.RefreshTokenDto;
import potatowoong.potatomallback.global.auth.jwt.dto.TokenDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.global.utils.CookieUtils;

@Service
@RequiredArgsConstructor
public class AdminLoginService {

    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    private final AdminLoginLogService adminLoginLogService;

    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "ADMIN_REFRESH_TOKEN";

    @Transactional(noRollbackFor = {CustomException.class})
    public AccessTokenDto login(LoginReqDto loginReqDto, HttpServletResponse response) {
        final String adminId = loginReqDto.id();
        final String password = loginReqDto.password();

        // ID로 Admin 조회
        Admin savedAdmin = adminRepository.findById(adminId)
            .orElseThrow(() -> {
                adminLoginLogService.addFailAdminLoginLog(adminId);
                return new CustomException(ErrorCode.FAILED_TO_LOGIN);
            });

        // Password 일치 여부 확인
        if (!passwordEncoder.matches(password, savedAdmin.getPassword())) {
            adminLoginLogService.addFailAdminLoginLog(adminId);
            throw new CustomException(ErrorCode.FAILED_TO_LOGIN);
        }

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminId, password, List.of(Role.ROLE_ADMIN::name));

        // 인증 정보를 기반으로 JWT Token 생성
        TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);
        RefreshTokenDto refreshTokenDto = tokenDto.refreshTokenDto();

        final long expiresInSecond = (refreshTokenDto.expiresIn() - System.currentTimeMillis()) / 1000;

        // Refresh Token을 Redis에 저장
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(tokenDto.refreshTokenDto().token(), adminId, expiresInSecond, TimeUnit.SECONDS);

        // Refresh Token을 쿠키에 담아서 전달
        Cookie cookie = CookieUtils.createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshTokenDto.token(), (int) expiresInSecond);
        response.addCookie(cookie);

        return tokenDto.accessTokenDto();
    }

    // TODO : 사용자와 관리자 공통으로 분리
    public AccessTokenDto refresh(HttpServletRequest request) {
        // Cookie에서 Refresh Token 가져오기
        final String refreshToken = CookieUtils.getCookieValue(request.getCookies(), REFRESH_TOKEN_COOKIE_NAME);
        if (StringUtils.isBlank(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // Redis에서 Refresh Token으로 Admin ID 가져오기
        String userName = redisTemplate.opsForValue().get(refreshToken);
        if (StringUtils.isBlank(userName)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(userName, "", List.of(Role.ROLE_ADMIN::name));

        // 인증 정보를 기반으로 JWT Token 생성
        return jwtTokenProvider.generateAccessToken(authentication);
    }
}

