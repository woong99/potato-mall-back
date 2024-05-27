package potatowoong.potatomallback.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import potatowoong.potatomallback.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.auth.service.AdminLoginService;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.jwt.dto.TokenDto;

@WebMvcTest(AdminLoginController.class)
@ExtendWith(MockitoExtension.class)
@WithMockUser
class AdminLoginControllerTest {

    @MockBean
    private AdminLoginService adminLoginService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("관리자 로그인 성공")
    void 로그인_성공() throws Exception {
        // given
        LoginReqDto loginReqDto = new LoginReqDto("adminId", "password");
        TokenDto tokenDto = new TokenDto("accessToken", "refreshToken");

        given(adminLoginService.login(any(LoginReqDto.class))).willReturn(tokenDto);

        // when & then
        mockMvc.perform(post("/api/admin/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReqDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value(tokenDto.accessToken()))
            .andExpect(jsonPath("$.data.refreshToken").value(tokenDto.refreshToken()));
    }

    @Test
    @DisplayName("관리자 로그인 실패 - 아이디 혹은 비밀번호 불일치")
    void 로그인_실패_아이디_불일치() throws Exception {
        // given
        LoginReqDto loginReqDto = new LoginReqDto("adminId", "password");

        given(adminLoginService.login(any(LoginReqDto.class))).willThrow(new CustomException(ErrorCode.FAILED_TO_LOGIN));

        // when & then
        mockMvc.perform(post("/api/admin/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReqDto)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_TO_LOGIN.getMessage()))
            .andExpect(jsonPath("$.code").value(ErrorCode.FAILED_TO_LOGIN.getCode()));
    }
}