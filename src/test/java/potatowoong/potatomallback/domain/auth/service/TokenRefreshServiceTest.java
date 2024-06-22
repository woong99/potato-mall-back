package potatowoong.potatomallback.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import jakarta.servlet.http.Cookie;
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
import potatowoong.potatomallback.domain.auth.enums.TokenName;
import potatowoong.potatomallback.global.auth.jwt.component.JwtTokenProvider;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class TokenRefreshServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private TokenRefreshService tokenRefreshService;

    @Nested
    @DisplayName("관리자 Access Token 갱신")
    class 관리자_Access_Token_갱신 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie(TokenName.ADMIN_REFRESH_TOKEN.name(), TokenName.ADMIN_REFRESH_TOKEN.name()));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(redisTemplate.opsForValue().get(TokenName.ADMIN_REFRESH_TOKEN.name())).willReturn("id");
            given(jwtTokenProvider.generateAccessToken(any())).willReturn(AccessTokenDto.builder().token("accessToken").build());

            // when
            AccessTokenDto result = tokenRefreshService.adminRefresh(request);

            // then
            assertThat(result.token()).isNotBlank();
            then(redisTemplate.opsForValue()).should().get(TokenName.ADMIN_REFRESH_TOKEN.name());
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
            assertThatThrownBy(() -> tokenRefreshService.adminRefresh(request))
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
            request.setCookies(new Cookie(TokenName.ADMIN_REFRESH_TOKEN.name(), TokenName.ADMIN_REFRESH_TOKEN.name()));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(redisTemplate.opsForValue().get(TokenName.ADMIN_REFRESH_TOKEN.name())).willReturn(null);

            // when
            assertThatThrownBy(() -> tokenRefreshService.adminRefresh(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

            // then
            then(redisTemplate.opsForValue()).should().get(TokenName.ADMIN_REFRESH_TOKEN.name());
            then(jwtTokenProvider).should(never()).generateAccessToken(any());
        }
    }

    @Nested
    @DisplayName("사용자 Access Token 갱신")
    class 사용자_Access_Token_갱신 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie(TokenName.USER_REFRESH_TOKEN.name(), TokenName.USER_REFRESH_TOKEN.name()));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(redisTemplate.opsForValue().get(TokenName.USER_REFRESH_TOKEN.name())).willReturn("id");
            given(jwtTokenProvider.generateAccessToken(any())).willReturn(AccessTokenDto.builder().token("accessToken").build());

            // when
            AccessTokenDto result = tokenRefreshService.userRefresh(request);

            // then
            assertThat(result.token()).isNotBlank();
            then(redisTemplate.opsForValue()).should().get(TokenName.USER_REFRESH_TOKEN.name());
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
            assertThatThrownBy(() -> tokenRefreshService.userRefresh(request))
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
            request.setCookies(new Cookie(TokenName.USER_REFRESH_TOKEN.name(), TokenName.USER_REFRESH_TOKEN.name()));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(redisTemplate.opsForValue().get(TokenName.USER_REFRESH_TOKEN.name())).willReturn(null);

            // when
            assertThatThrownBy(() -> tokenRefreshService.userRefresh(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

            // then
            then(redisTemplate.opsForValue()).should().get(TokenName.USER_REFRESH_TOKEN.name());
            then(jwtTokenProvider).should(never()).generateAccessToken(any());
        }
    }
}