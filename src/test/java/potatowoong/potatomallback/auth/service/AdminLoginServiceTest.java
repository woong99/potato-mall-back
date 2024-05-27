package potatowoong.potatomallback.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import potatowoong.potatomallback.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.auth.entity.Admin;
import potatowoong.potatomallback.auth.repository.AdminRepository;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.jwt.component.JwtTokenProvider;
import potatowoong.potatomallback.jwt.dto.TokenDto;

@ExtendWith(MockitoExtension.class)
class AdminLoginServiceTest {

    private final String userId = "id";

    private final String password = "password";

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AdminLoginLogService adminLoginLogService;

    @InjectMocks
    private AdminLoginService adminLoginService;

    private LoginReqDto loginReqDto;

    private Admin admin;

    @BeforeEach
    void setUp() {
        loginReqDto = new LoginReqDto(userId, password);
        admin = Admin.builder()
            .adminId(userId)
            .password(password)
            .build();
    }

    @Test
    @DisplayName("관리자 로그인 성공")
    void 로그인_성공() {
        // given
        TokenDto tokenDto = new TokenDto("accessToken", "refreshToken");

        given(adminRepository.findById(userId)).willReturn(Optional.ofNullable(admin));
        given(passwordEncoder.matches(loginReqDto.password(), admin.getPassword())).willReturn(true);
        given(jwtTokenProvider.generateToken(any())).willReturn(tokenDto);

        // when
        TokenDto result = adminLoginService.login(loginReqDto);

        // then
        assertThat(result).isNotNull();
        then(adminRepository).should().findById(userId);
        then(passwordEncoder).should().matches(password, password);
        then(jwtTokenProvider).should().generateToken(any());
    }

    @Test
    @DisplayName("관리자 로그인 실패 - 아이디 불일치")
    void 로그인_실패_아이디_불일치() {
        // given
        given(adminRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminLoginService.login(loginReqDto))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_LOGIN);
        then(adminRepository).should().findById(userId);
        then(passwordEncoder).shouldHaveNoInteractions();
        then(jwtTokenProvider).shouldHaveNoInteractions();
        then(adminLoginLogService).should().addFailAdminLoginLog(userId);
    }

    @Test
    @DisplayName("관리자 로그인 실패 - 비밀번호 불일치")
    void 로그인_실패_비밀번호_불일치() {
        // given
        given(adminRepository.findById("id")).willReturn(Optional.ofNullable(admin));
        given(passwordEncoder.matches(password, password)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> adminLoginService.login(loginReqDto))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_LOGIN);
        then(adminRepository).should().findById(userId);
        then(passwordEncoder).should().matches(password, password);
        then(jwtTokenProvider).shouldHaveNoInteractions();
        then(adminLoginLogService).should().addFailAdminLoginLog(userId);
    }
}