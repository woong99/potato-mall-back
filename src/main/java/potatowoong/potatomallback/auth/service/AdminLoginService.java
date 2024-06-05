package potatowoong.potatomallback.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
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
import potatowoong.potatomallback.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.auth.entity.Admin;
import potatowoong.potatomallback.auth.enums.Role;
import potatowoong.potatomallback.auth.repository.AdminRepository;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.jwt.component.JwtTokenProvider;
import potatowoong.potatomallback.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.jwt.dto.TokenDto;

@Service
@RequiredArgsConstructor
public class AdminLoginService {

    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    private final AdminLoginLogService adminLoginLogService;

    private final StringRedisTemplate redisTemplate;

    @Transactional(noRollbackFor = {CustomException.class})
    public TokenDto login(LoginReqDto loginReqDto) {
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

        // Refresh Token을 Redis에 저장
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        long refreshTokenExpiredIn = Duration.between(LocalDateTime.now(), tokenDto.refreshTokenDto().tokenExpiresIn()).getSeconds();
        valueOperations.set(tokenDto.refreshTokenDto().token(), adminId, refreshTokenExpiredIn, TimeUnit.SECONDS);

        return tokenDto;
    }

    public AccessTokenDto refresh(HttpServletRequest request) {
        // Cookie에서 Refresh Token 가져오기
        Cookie[] cookies = request.getCookies();
        String refreshToken = Arrays.stream(cookies).
            filter(cookie -> cookie.getName().equals("refreshToken"))
            .findFirst()
            .map(Cookie::getValue)
            .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

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

