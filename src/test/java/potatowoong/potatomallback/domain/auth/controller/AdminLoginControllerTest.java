package potatowoong.potatomallback.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentRequest;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getNoAuthDocumentRequest;

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
import potatowoong.potatomallback.domain.auth.service.AdminLoginLogService;
import potatowoong.potatomallback.domain.auth.service.AdminLoginService;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@WebMvcTest(controllers = AdminLoginController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class AdminLoginControllerTest {

    private final String adminId = "adminId";

    @MockBean
    private AdminLoginService adminLoginService;

    @MockBean
    private AdminLoginLogService adminLoginLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("관리자 로그인")
    class 관리자_로그인 {

        @Test
        @DisplayName("성공")
        void 로그인_성공() throws Exception {
            // given
            LoginReqDto loginReqDto = new LoginReqDto(adminId, "password");
            AccessTokenDto accessTokenDto = AccessTokenDto.builder()
                .token("accessToken")
                .expiresIn(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();

            given(adminLoginService.login(any(LoginReqDto.class), any(HttpServletResponse.class))).willReturn(accessTokenDto);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/login")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReqDto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value(accessTokenDto.token()))
                .andExpect(jsonPath("$.data.expiresIn").value(accessTokenDto.expiresIn()));

            actions
                .andDo(document("admin-login",
                        getNoAuthDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                            fieldWithPath("id").type(JsonFieldType.STRING).description("아이디").optional(),
                            fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호").optional()
                        ),
                        responseFields(
                            beneathPath("data").withSubsectionId("data"),
                            fieldWithPath("token").description("액세스 토큰"),
                            fieldWithPath("expiresIn").description("액세스 토큰 만료 시간")
                        )
                    )
                );

            then(adminLoginService).should().login(any(LoginReqDto.class), any(HttpServletResponse.class));
            then(adminLoginLogService).should().addSuccessAdminLoginLog(loginReqDto.id());
        }

        @Test
        @DisplayName("실패 - 아이디 혹은 비밀번호 불일치")
        void 로그인_실패_아이디_불일치() throws Exception {
            // given
            LoginReqDto loginReqDto = new LoginReqDto(adminId, "invalidPassword");

            given(adminLoginService.login(any(LoginReqDto.class), any(HttpServletResponse.class))).willThrow(new CustomException(ErrorCode.FAILED_TO_LOGIN));

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/login")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReqDto)));

            actions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_TO_LOGIN.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.FAILED_TO_LOGIN.getCode()));

            actions
                .andDo(document("admin-login-fail",
                        getNoAuthDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                            fieldWithPath("id").type(JsonFieldType.STRING).description("아이디").optional(),
                            fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호").optional()
                        )
                    )
                );

            then(adminLoginService).should().login(any(LoginReqDto.class), any(HttpServletResponse.class));
        }
    }

    @Nested
    @DisplayName("관리자 로그아웃")
    @WithMockUser("adminId")
    class 관리자_로그아웃 {

        @Test
        @DisplayName("성공")
        void 로그아웃_성공() throws Exception {
            // given
            willDoNothing().given(adminLoginLogService).addLogoutAdminLoginLog(adminId);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/logout")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("로그아웃 성공"));

            actions
                .andDo(document("admin-logout",
                        getDocumentRequest(),
                        getDocumentResponse()
                    )
                );

            then(adminLoginLogService).should().addLogoutAdminLoginLog(adminId);
        }
    }

    @Nested
    @DisplayName("Access Token 갱신")
    class Access_Token_갱신 {

        @Test
        @DisplayName("성공")
        void 갱신_성공() throws Exception {
            // given
            AccessTokenDto accessTokenDto = AccessTokenDto.builder()
                .token("accessToken")
                .expiresIn(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();

            given(adminLoginService.refresh(any())).willReturn(accessTokenDto);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/refresh")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value(accessTokenDto.token()));

            actions
                .andDo(document("refresh-access-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                            beneathPath("data").withSubsectionId("data"),
                            fieldWithPath("token").description("액세스 토큰"),
                            fieldWithPath("expiresIn").description("액세스 토큰 만료 시간")
                        )
                    )
                );

            then(adminLoginService).should().refresh(any());
        }

        @Test
        @DisplayName("실패 - Refresh Token이 존재하지 않음")
        void 갱신_실패_Refresh_Token_존재하지_않음() throws Exception {
            // given
            given(adminLoginService.refresh(any())).willThrow(new CustomException(ErrorCode.UNAUTHORIZED));

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/refresh")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));

            actions
                .andDo(document("refresh-access-token-fail",
                        getDocumentRequest(),
                        getDocumentResponse()
                    )
                );

            then(adminLoginService).should().refresh(any());
        }
    }
}