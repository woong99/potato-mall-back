package potatowoong.potatomallback.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentRequest;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.domain.auth.dto.request.UserSignUpReqDto;
import potatowoong.potatomallback.domain.auth.service.TokenRefreshService;
import potatowoong.potatomallback.domain.auth.service.UserLoginService;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.common.ResponseText;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@WebMvcTest(controllers = UserLoginController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class UserLoginControllerTest {

    @MockBean
    private UserLoginService userLoginService;

    @MockBean
    private TokenRefreshService tokenRefreshService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("로그인")
    class 로그인 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            LoginReqDto loginReqDto = LoginReqDto.builder()
                .id("userId")
                .password("password")
                .build();
            AccessTokenDto accessTokenDto = AccessTokenDto.builder()
                .token("accessToken")
                .expiresIn(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();

            given(userLoginService.login(any(LoginReqDto.class), any(HttpServletResponse.class))).willReturn(accessTokenDto);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/login")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(loginReqDto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value(accessTokenDto.token()))
                .andExpect(jsonPath("$.data.expiresIn").value(accessTokenDto.expiresIn()));

            actions
                .andDo(document("user-login",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                            fieldWithPath("id").type(JsonFieldType.STRING).optional().description("아이디"),
                            fieldWithPath("password").type(JsonFieldType.STRING).optional().description("비밀번호")
                        ),
                        responseFields(
                            beneathPath("data").withSubsectionId("data"),
                            fieldWithPath("token").type(JsonFieldType.STRING).description("액세스 토큰"),
                            fieldWithPath("expiresIn").type(JsonFieldType.NUMBER).description("액세스 토큰 만료 시간")
                        )
                    )
                );

            then(userLoginService).should().login(any(LoginReqDto.class), any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("실패 - 아이디 혹은 비밀번호 불일치")
        void 실패_아이디_혹은_비밀번호_불일치() throws Exception {
            // given
            LoginReqDto loginReqDto = LoginReqDto.builder()
                .id("userId")
                .password("invalidPassword")
                .build();

            given(userLoginService.login(any(LoginReqDto.class), any(HttpServletResponse.class))).willThrow(new CustomException(ErrorCode.FAILED_TO_LOGIN));

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/login")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(loginReqDto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_TO_LOGIN.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.FAILED_TO_LOGIN.getCode()));

            actions
                .andDo(document("user-login-fail",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                            fieldWithPath("id").type(JsonFieldType.STRING).optional().description("아이디"),
                            fieldWithPath("password").type(JsonFieldType.STRING).optional().description("비밀번호")
                        )
                    )
                );

            then(userLoginService).should().login(any(LoginReqDto.class), any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("실패 - 소셜 로그인 사용자인 경우")
        void 실패_소셜_로그인_사용자인_경우() throws Exception {
            // given
            LoginReqDto loginReqDto = LoginReqDto.builder()
                .id("userId")
                .password("password")
                .build();

            given(userLoginService.login(any(LoginReqDto.class), any(HttpServletResponse.class))).willThrow(new CustomException(ErrorCode.WRONG_LOGIN_TYPE));

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/login")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(loginReqDto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.WRONG_LOGIN_TYPE.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.WRONG_LOGIN_TYPE.getCode()));

            actions
                .andDo(document("user-login-fail-wrong-login-type",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                            fieldWithPath("id").type(JsonFieldType.STRING).optional().description("아이디"),
                            fieldWithPath("password").type(JsonFieldType.STRING).optional().description("비밀번호")
                        )
                    )
                );

            then(userLoginService).should().login(any(LoginReqDto.class), any(HttpServletResponse.class));
        }
    }

    @Nested
    @DisplayName("회원가입")
    class 회원가입 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            UserSignUpReqDto dto = getUserSignUpReqDto();

            willDoNothing().given(userLoginService).signUp(dto);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/sign-up")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_SIGN_UP));

            actions
                .andDo(document("user-sign-up",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                            fieldWithPath("userId").type(JsonFieldType.STRING).optional().description("아이디"),
                            fieldWithPath("password").type(JsonFieldType.STRING).optional().description("비밀번호"),
                            fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).optional().description("비밀번호 확인"),
                            fieldWithPath("nickname").type(JsonFieldType.STRING).optional().description("닉네임")
                        )
                    )
                );

            then(userLoginService).should().signUp(any(UserSignUpReqDto.class));
        }

        @Test
        @DisplayName("실패 - 아이디 중복")
        void 실패_아이디_중복() throws Exception {
            // given
            UserSignUpReqDto dto = getUserSignUpReqDto();

            willThrow(new CustomException(ErrorCode.DUPLICATE_USER_ID)).given(userLoginService).signUp(any());

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/sign-up")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_USER_ID.getMessage()));

            actions
                .andDo(document("user-sign-up-fail-duplicate-id",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                            fieldWithPath("userId").type(JsonFieldType.STRING).optional().description("아이디"),
                            fieldWithPath("password").type(JsonFieldType.STRING).optional().description("비밀번호"),
                            fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).optional().description("비밀번호 확인"),
                            fieldWithPath("nickname").type(JsonFieldType.STRING).optional().description("닉네임")
                        )
                    )
                );

            then(userLoginService).should().signUp(any(UserSignUpReqDto.class));
        }

        @Test
        @DisplayName("실패 - 닉네임 중복")
        void 실패_닉네임_중복() throws Exception {
            // given
            UserSignUpReqDto dto = getUserSignUpReqDto();

            willThrow(new CustomException(ErrorCode.DUPLICATE_NICKNAME)).given(userLoginService).signUp(any());

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/sign-up")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_NICKNAME.getMessage()));

            actions
                .andDo(document("user-sign-up-fail-duplicate-nickname",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                            fieldWithPath("userId").type(JsonFieldType.STRING).optional().description("아이디"),
                            fieldWithPath("password").type(JsonFieldType.STRING).optional().description("비밀번호"),
                            fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).optional().description("비밀번호 확인"),
                            fieldWithPath("nickname").type(JsonFieldType.STRING).optional().description("닉네임")
                        )
                    )
                );

            then(userLoginService).should().signUp(any(UserSignUpReqDto.class));
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void 실패_비밀번호_불일치() throws Exception {
            // given
            UserSignUpReqDto dto = getUserSignUpReqDto();

            willThrow(new CustomException(ErrorCode.PASSWORD_NOT_MATCHED)).given(userLoginService).signUp(any());

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/sign-up")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_NOT_MATCHED.getMessage()));

            actions
                .andDo(document("user-sign-up-fail-password-not-matched",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                            fieldWithPath("userId").type(JsonFieldType.STRING).optional().description("아이디"),
                            fieldWithPath("password").type(JsonFieldType.STRING).optional().description("비밀번호"),
                            fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).optional().description("비밀번호 확인"),
                            fieldWithPath("nickname").type(JsonFieldType.STRING).optional().description("닉네임")
                        )
                    )
                );

            then(userLoginService).should().signUp(any(UserSignUpReqDto.class));
        }

        private UserSignUpReqDto getUserSignUpReqDto() {
            return UserSignUpReqDto.builder()
                .userId("userId")
                .password("password")
                .passwordConfirm("password")
                .nickname("nickname")
                .build();
        }
    }

    @Nested
    @DisplayName("아이디 중복 체크")
    class 아이디_중복_체크 {

        private final String userId = "userId";

        @Test
        @DisplayName("중복된 아이디가 존재하는 경우")
        void 중복된_아이디가_존재하는_경우() throws Exception {
            // given
            given(userLoginService.checkDuplicateId(userId)).willReturn(true);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/check-duplicate-id")
                .with(csrf().asHeader())
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.DUPLICATE));

            actions
                .andDo(document("user-check-duplicate-id-duplicate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                            parameterWithName("userId").optional().description("아이디")
                        )
                    )
                );

            then(userLoginService).should().checkDuplicateId(userId);
        }

        @Test
        @DisplayName("중복된 아이디가 존재하지 않는 경우")
        void 중복된_아이디가_존재하지_않는_경우() throws Exception {
            // given
            given(userLoginService.checkDuplicateId(userId)).willReturn(false);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/check-duplicate-id")
                .with(csrf().asHeader())
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.OK));

            actions
                .andDo(document("user-check-duplicate-id-not-duplicate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                            parameterWithName("userId").optional().description("아이디")
                        )
                    )
                );

            then(userLoginService).should().checkDuplicateId(userId);
        }
    }

    @Nested
    @DisplayName("닉네임 중복 체크")
    class 닉네임_중복_체크 {

        private final String nickname = "nickname";

        @Test
        @DisplayName("중복된 닉네임이 존재하는 경우")
        void 중복된_닉네임이_존재하는_경우() throws Exception {
            // given
            given(userLoginService.checkDuplicateNickname(nickname)).willReturn(true);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/check-duplicate-nickname")
                .with(csrf().asHeader())
                .param("nickname", nickname)
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.DUPLICATE));

            actions
                .andDo(document("user-check-duplicate-nickname-duplicate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                            parameterWithName("nickname").optional().description("닉네임")
                        )
                    )
                );

            then(userLoginService).should().checkDuplicateNickname(nickname);
        }

        @Test
        @DisplayName("중복된 닉네임이 존재하지 않는 경우")
        void 중복된_닉네임이_존재하지_않는_경우() throws Exception {
            // given
            given(userLoginService.checkDuplicateNickname(nickname)).willReturn(false);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/check-duplicate-nickname")
                .with(csrf().asHeader())
                .param("nickname", nickname)
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.OK));

            actions
                .andDo(document("user-check-duplicate-nickname-not-duplicate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                            parameterWithName("nickname").optional().description("닉네임")
                        )
                    )
                );

            then(userLoginService).should().checkDuplicateNickname(nickname);
        }
    }

    @Nested
    @DisplayName("사용자 Access Token 갱신")
    class Access_Token_갱신 {

        @Test
        @DisplayName("성공")
        void 갱신_성공() throws Exception {
            // given
            AccessTokenDto accessTokenDto = AccessTokenDto.builder()
                .token("accessToken")
                .expiresIn(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();

            given(tokenRefreshService.userRefresh(any())).willReturn(accessTokenDto);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/refresh")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value(accessTokenDto.token()));

            actions
                .andDo(document("user-refresh-access-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                            beneathPath("data").withSubsectionId("data"),
                            fieldWithPath("token").description("액세스 토큰"),
                            fieldWithPath("expiresIn").description("액세스 토큰 만료 시간")
                        )
                    )
                );

            then(tokenRefreshService).should().userRefresh(any());
        }

        @Test
        @DisplayName("실패 - Refresh Token이 존재하지 않음")
        void 갱신_실패_Refresh_Token_존재하지_않음() throws Exception {
            // given
            given(tokenRefreshService.userRefresh(any())).willThrow(new CustomException(ErrorCode.UNAUTHORIZED));

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/refresh")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));

            actions
                .andDo(document("user-refresh-access-token-fail",
                        getDocumentRequest(),
                        getDocumentResponse()
                    )
                );

            then(tokenRefreshService).should().userRefresh(any());
        }
    }
}