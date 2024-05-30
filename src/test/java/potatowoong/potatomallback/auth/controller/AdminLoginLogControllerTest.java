package potatowoong.potatomallback.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentRequest;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import org.hibernate.query.SortDirection;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.auth.dto.response.AdminLoginLogResDto;
import potatowoong.potatomallback.auth.enums.TryResult;
import potatowoong.potatomallback.auth.service.AdminLoginLogService;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.config.security.PortCheckFilter;

@WebMvcTest(controllers = AdminLoginLogController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PortCheckFilter.class)})
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
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
            .searchWord("admin")
            .searchCondition("adminId")
            .sortDirection(SortDirection.DESCENDING)
            .sortCondition("tryDate")
            .build();
        AdminLoginLogResDto adminLoginLogResDto = AdminLoginLogResDto.builder()
            .adminLoginLogId(1L)
            .adminId("admin")
            .tryIp("127.0.0.1")
            .tryResult(TryResult.SUCCESS)
            .tryDate(LocalDateTime.now())

            .build();
        PageResponseDto<AdminLoginLogResDto> pageResponseDto = new PageResponseDto<>(Collections.singletonList(adminLoginLogResDto), 1);

        given(adminLoginLogService.getAdminLoginLogWithPage(pageRequestDto)).willReturn(pageResponseDto);

        // when & then
        ResultActions actions = mockMvc.perform(get("/api/admin/login-logs")
            .with(csrf().asHeader())
            .param("page", "0")
            .param("size", "10")
            .param("searchWord", "admin")
            .param("searchCondition", "adminId")
            .param("sortDirection", "DESCENDING")
            .param("sortCondition", "tryDate")
        );

        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.result").exists())
            .andExpect(jsonPath("$.data.totalElements").value(1));

        actions
            .andDo(document("admin-login-logs",
                getDocumentRequest(),
                getDocumentResponse(),
                queryParameters(
                    parameterWithName("page").description("페이지 번호"),
                    parameterWithName("size").description("페이지 크기"),
                    parameterWithName("searchWord").description("검색어"),
                    parameterWithName("searchCondition").description("검색 조건(adminId: 관리자 ID, tryIp: 로그인 시도 IP)"),
                    parameterWithName("sortDirection").description("정렬 방향(DESCENDING: 내림차순, ASCENDING: 오름차순)"),
                    parameterWithName("sortCondition").description("정렬 조건(tryDate: 로그인 시도 일시, adminId: 관리자 ID, tryResult: 로그인 시도 결과)")
                ),
                responseFields(
                    beneathPath("data").withSubsectionId("data"),
                    fieldWithPath("result[].adminLoginLogId").type(JsonFieldType.NUMBER).description("관리자 로그인 내역 정보 ID"),
                    fieldWithPath("result[].adminId").type(JsonFieldType.STRING).description("관리자 ID"),
                    fieldWithPath("result[].tryIp").type(JsonFieldType.STRING).description("로그인 시도 IP"),
                    fieldWithPath("result[].tryResult").type(JsonFieldType.STRING).description("로그인 시도 결과(SUCCESS: 성공, FAIL: 실패, LOGOUT: 로그아웃)"),
                    fieldWithPath("result[].tryDate").type(JsonFieldType.STRING).description("로그인 시도 일시(yyyy-MM-ddTHH:mm:ss)"),
                    fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("전체 로그인 내역 수")
                ))
            );
    }
}