package potatowoong.potatomallback.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentRequest;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.auth.service.TokenRefreshService;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@WebMvcTest(controllers = UserLoginController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class UserLoginControllerTest {

    @MockBean
    private TokenRefreshService tokenRefreshService;

    @Autowired
    private MockMvc mockMvc;

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