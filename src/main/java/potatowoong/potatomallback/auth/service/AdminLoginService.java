package potatowoong.potatomallback.auth.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
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
import potatowoong.potatomallback.jwt.dto.TokenDto;

@Service
@RequiredArgsConstructor
public class AdminLoginService {

    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public TokenDto login(LoginReqDto loginReqDto) {
        final String userId = loginReqDto.id();
        final String password = loginReqDto.password();

        // ID로 Admin 조회
        Admin savedAdmin = adminRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.FAILED_TO_LOGIN));

        // Password 일치 여부 확인
        if (!passwordEncoder.matches(password, savedAdmin.getPassword())) {
            throw new CustomException(ErrorCode.FAILED_TO_LOGIN);
        }

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, password, List.of(Role.ROLE_ADMIN::name));

        // 인증 정보를 기반으로 JWT Token 생성
        return jwtTokenProvider.generateToken(authentication);
    }
}

