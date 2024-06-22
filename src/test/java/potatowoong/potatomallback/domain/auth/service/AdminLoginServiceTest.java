package potatowoong.potatomallback.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import potatowoong.potatomallback.domain.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.domain.auth.entity.Admin;
import potatowoong.potatomallback.domain.auth.repository.AdminRepository;
import potatowoong.potatomallback.global.auth.jwt.component.JwtTokenProvider;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.auth.jwt.dto.RefreshTokenDto;
import potatowoong.potatomallback.global.auth.jwt.dto.TokenDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

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

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

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

    @Nested
    @DisplayName("관리자 로그인")
    class 관리자_로그인 {

        @Test
        @DisplayName("성공")
        void 로그인_성공() {
            // given
            final long tokenExpiresIn = LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            AccessTokenDto accessTokenDto = AccessTokenDto.builder()
                .token("accessToken")
                .expiresIn(tokenExpiresIn)
                .build();
            RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                .token("refreshToken")
                .expiresIn(tokenExpiresIn)
                .build();
            TokenDto tokenDto = new TokenDto(accessTokenDto, refreshTokenDto);
            MockHttpServletResponse response = new MockHttpServletResponse();

            given(adminRepository.findById(userId)).willReturn(Optional.ofNullable(admin));
            given(passwordEncoder.matches(loginReqDto.password(), admin.getPassword())).willReturn(true);
            given(jwtTokenProvider.generateToken(any())).willReturn(tokenDto);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            willDoNothing().given(valueOperations).set(eq(refreshTokenDto.token()), eq(userId), any(Long.class), eq(TimeUnit.SECONDS));

            // when
            AccessTokenDto result = adminLoginService.login(loginReqDto, response);

            // then
            assertThat(result).isNotNull();
            then(adminRepository).should().findById(userId);
            then(passwordEncoder).should().matches(password, password);
            then(jwtTokenProvider).should().generateToken(any());
            then(redisTemplate).should().opsForValue();
            then(valueOperations).should().set(eq(refreshTokenDto.token()), eq(userId), any(Long.class), eq(TimeUnit.SECONDS));
            then(adminLoginLogService).should(never()).addFailAdminLoginLog(userId);
        }

        @Test
        @DisplayName("실패 - 아이디 불일치")
        void 로그인_실패_아이디_불일치() {
            // given
            MockHttpServletResponse response = new MockHttpServletResponse();

            given(adminRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminLoginService.login(loginReqDto, response))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_LOGIN);
            then(adminRepository).should().findById(userId);
            then(passwordEncoder).shouldHaveNoInteractions();
            then(jwtTokenProvider).shouldHaveNoInteractions();
            then(adminLoginLogService).should().addFailAdminLoginLog(userId);
            then(redisTemplate).shouldHaveNoInteractions();
            then(valueOperations).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void 로그인_실패_비밀번호_불일치() {
            // given
            MockHttpServletResponse response = new MockHttpServletResponse();

            given(adminRepository.findById("id")).willReturn(Optional.ofNullable(admin));
            given(passwordEncoder.matches(password, password)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> adminLoginService.login(loginReqDto, response))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_LOGIN);
            then(adminRepository).should().findById(userId);
            then(passwordEncoder).should().matches(password, password);
            then(jwtTokenProvider).shouldHaveNoInteractions();
            then(adminLoginLogService).should().addFailAdminLoginLog(userId);
            then(redisTemplate).shouldHaveNoInteractions();
            then(valueOperations).shouldHaveNoInteractions();
        }
    }
}