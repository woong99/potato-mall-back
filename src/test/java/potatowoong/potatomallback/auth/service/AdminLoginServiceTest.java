package potatowoong.potatomallback.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import potatowoong.potatomallback.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.auth.entity.Admin;
import potatowoong.potatomallback.auth.repository.AdminRepository;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.jwt.component.JwtTokenProvider;
import potatowoong.potatomallback.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.jwt.dto.RefreshTokenDto;
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
            AccessTokenDto accessTokenDto = AccessTokenDto.builder()
                .token("accessToken")
                .build();
            RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                .token("refreshToken")
                .tokenExpiresIn(LocalDateTime.now())
                .build();
            TokenDto tokenDto = new TokenDto(accessTokenDto, refreshTokenDto);

            given(adminRepository.findById(userId)).willReturn(Optional.ofNullable(admin));
            given(passwordEncoder.matches(loginReqDto.password(), admin.getPassword())).willReturn(true);
            given(jwtTokenProvider.generateToken(any())).willReturn(tokenDto);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            willDoNothing().given(valueOperations).set(refreshTokenDto.token(), userId, -1L, TimeUnit.SECONDS);

            // when
            TokenDto result = adminLoginService.login(loginReqDto);

            // then
            assertThat(result).isNotNull();
            then(adminRepository).should().findById(userId);
            then(passwordEncoder).should().matches(password, password);
            then(jwtTokenProvider).should().generateToken(any());
            then(redisTemplate).should().opsForValue();
            then(valueOperations).should().set(refreshTokenDto.token(), userId, -1L, TimeUnit.SECONDS);
            then(adminLoginLogService).should(never()).addFailAdminLoginLog(userId);
        }

        @Test
        @DisplayName("실패 - 아이디 불일치")
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
            then(redisTemplate).shouldHaveNoInteractions();
            then(valueOperations).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
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
            then(redisTemplate).shouldHaveNoInteractions();
            then(valueOperations).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Access Token 갱신")
    class Access_Token_갱신 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("refreshToken", "refreshToken"));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(redisTemplate.opsForValue().get("refreshToken")).willReturn(userId);
            given(jwtTokenProvider.generateAccessToken(any())).willReturn(AccessTokenDto.builder().token("accessToken").build());

            // when
            AccessTokenDto result = adminLoginService.refresh(request);

            // then
            assertThat(result.token()).isNotBlank();
            then(redisTemplate.opsForValue()).should().get("refreshToken");
            then(jwtTokenProvider).should().generateAccessToken(any());
        }

        @Test
        @DisplayName("실패 - 쿠키에 Refresh Token이 존재하지 않음")
        void 실패_Refresh_Token_존재하지_않음() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("invalidToken", "invalidToken"));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            assertThatThrownBy(() -> adminLoginService.refresh(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

            // then
            then(redisTemplate.opsForValue()).should(never()).get("refreshToken");
            then(jwtTokenProvider).should(never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("실패 - Redis에 Refresh Token이 존재하지 않음")
        void 실패_Redis에_Refresh_Token_존재하지_않음() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("refreshToken", "refreshToken"));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(redisTemplate.opsForValue().get("refreshToken")).willReturn(null);

            // when
            assertThatThrownBy(() -> adminLoginService.refresh(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

            // then
            then(redisTemplate.opsForValue()).should().get("refreshToken");
            then(jwtTokenProvider).should(never()).generateAccessToken(any());
        }
    }
}