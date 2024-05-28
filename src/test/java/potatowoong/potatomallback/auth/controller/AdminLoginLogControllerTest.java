package potatowoong.potatomallback.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import potatowoong.potatomallback.auth.dto.response.AdminLoginLogResDto;
import potatowoong.potatomallback.auth.service.AdminLoginLogService;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;

@WebMvcTest(AdminLoginLogController.class)
@ExtendWith(MockitoExtension.class)
@WithMockUser
class AdminLoginLogControllerTest {

    @MockBean
    private AdminLoginLogService adminLoginLogService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("관리자 로그인 내역 목록 조회 성공")
    void 로그인_내역_목록_조회_성공() throws Exception {
        // given
        PageRequestDto pageRequestDto = PageRequestDto.builder()
            .page(0)
            .size(10)
            .build();
        AdminLoginLogResDto adminLoginLogResDto = AdminLoginLogResDto.builder()
            .build();
        PageResponseDto<AdminLoginLogResDto> pageResponseDto = new PageResponseDto<>(Collections.singletonList(adminLoginLogResDto), 1);

        given(adminLoginLogService.getAdminLoginLogWithPage(pageRequestDto)).willReturn(pageResponseDto);

        // when & then
        mockMvc.perform(get("/api/admin/login-logs")
                .with(csrf())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.result").exists())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}