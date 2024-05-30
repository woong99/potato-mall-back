package potatowoong.potatomallback.auth.controller;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.auth.service.AdminLoginLogService;
import potatowoong.potatomallback.auth.service.AdminLoginService;
import potatowoong.potatomallback.config.security.PortCheckFilter;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.jwt.dto.TokenDto;

@WebMvcTest(controllers = AdminLoginController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PortCheckFilter.class)})
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

    @Test
    @DisplayName("관리자 로그인 성공")
    void 로그인_성공() throws Exception {
        // given
        LoginReqDto loginReqDto = new LoginReqDto(adminId, "password");
        TokenDto tokenDto = new TokenDto("accessToken", "refreshToken");

        given(adminLoginService.login(any(LoginReqDto.class))).willReturn(tokenDto);

        // when & then
        ResultActions actions = mockMvc.perform(post("/api/admin/login")
            .with(csrf().asHeader())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginReqDto)));

        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value(tokenDto.accessToken()))
            .andExpect(jsonPath("$.data.refreshToken").value(tokenDto.refreshToken()));

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
                        fieldWithPath("accessToken").description("액세스 토큰"),
                        fieldWithPath("refreshToken").description("리프레시 토큰")
                    )
                )
            );

        then(adminLoginService).should().login(loginReqDto);
        then(adminLoginLogService).should().addSuccessAdminLoginLog(loginReqDto.id());
    }

    @Test
    @DisplayName("관리자 로그인 실패 - 아이디 혹은 비밀번호 불일치")
    void 로그인_실패_아이디_불일치() throws Exception {
        // given
        LoginReqDto loginReqDto = new LoginReqDto(adminId, "invalidPassword");

        given(adminLoginService.login(any(LoginReqDto.class))).willThrow(new CustomException(ErrorCode.FAILED_TO_LOGIN));

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

        then(adminLoginService).should().login(loginReqDto);
    }

    @Test
    @DisplayName("관리자 로그아웃 성공")
    @WithMockUser("adminId")
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