package potatowoong.potatomallback.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentRequest;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.auth.dto.request.AdminAddReqDto;
import potatowoong.potatomallback.auth.dto.request.AdminModifyReqDto;
import potatowoong.potatomallback.auth.dto.response.AdminResDto;
import potatowoong.potatomallback.auth.service.AdminLogService;
import potatowoong.potatomallback.auth.service.AdminManageService;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.config.db.UseFlag;
import potatowoong.potatomallback.config.security.PortCheckFilter;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;

@WebMvcTest(controllers = AdminManageController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PortCheckFilter.class)})
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class AdminManageControllerTest {

    @MockBean
    private AdminManageService adminManageService;

    @MockBean
    private AdminLogService adminLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final String adminId = "adminId";

    private final String name = "name";

    private final String password = "password";

    @Nested
    @DisplayName("관리자 목록 조회")
    class 관리자_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            PageRequestDto pageRequestDto = PageRequestDto.builder()
                .page(0)
                .size(10)
                .searchWord("admin")
                .searchCondition("adminId")
                .sortDirection(SortDirection.DESCENDING)
                .sortCondition("tryDate")
                .build();
            AdminResDto adminResDto = AdminResDto.builder()
                .adminId("admin")
                .name("관리자")
                .updatedAt(LocalDateTime.now())
                .useFlag(UseFlag.Y)
                .build();
            PageResponseDto<AdminResDto> pageResponseDto = new PageResponseDto<>(List.of(adminResDto), 1);
            given(adminManageService.getAdminList(pageRequestDto)).willReturn(pageResponseDto);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/admin/admin-management/search")
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
                .andDo(document("admin-management-list",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    queryParameters(
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기"),
                        parameterWithName("searchWord").description("검색어"),
                        parameterWithName("searchCondition").description("검색 조건(admin : 관리자 ID, name: 이름)"),
                        parameterWithName("sortDirection").description("정렬 방향(DESCENDING: 내림차순, ASCENDING: 오름차순)"),
                        parameterWithName("sortCondition").description("정렬 조건(adminId: 관리자 ID, name: 이름, updatedAt: 최종 수정 일시)")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("result[].adminId").type(JsonFieldType.STRING).description("관리자 ID"),
                        fieldWithPath("result[].name").type(JsonFieldType.STRING).description("이름"),
                        fieldWithPath("result[].updatedAt").type(JsonFieldType.STRING).description("최종 수정 일시"),
                        fieldWithPath("result[].useFlag").type(JsonFieldType.STRING).description("사용여부(Y/N)"),
                        fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("전체 관리자 수")
                    )
                ));

            then(adminManageService).should().getAdminList(pageRequestDto);
            then(adminLogService).should().addAdminLog(any(), any());
        }
    }

    @Nested
    @DisplayName("관리자 상세 조회")
    class 관리자_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            given(adminManageService.getAdmin(adminId)).willReturn(AdminResDto.builder()
                .adminId(adminId)
                .name(name)
                .updatedAt(LocalDateTime.now())
                .useFlag(UseFlag.Y)
                .build());

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/admin/admin-management/{adminId}", adminId)
                .with(csrf().asHeader()));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adminId").value(adminId))
                .andExpect(jsonPath("$.data.name").value(name))
                .andExpect(jsonPath("$.data.updatedAt").exists())
                .andExpect(jsonPath("$.data.useFlag").value(UseFlag.Y.name()));

            actions
                .andDo(document("admin-management-detail",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("adminId").description("관리자 ID")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("adminId").type(JsonFieldType.STRING).description("관리자 ID"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                        fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("최종 수정 일시"),
                        fieldWithPath("useFlag").type(JsonFieldType.STRING).description("사용여부(Y/N)")
                    )
                ));

            then(adminManageService).should().getAdmin(adminId);
            then(adminLogService).should().addAdminLog(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 잘못된 adminId를 입력한 경우")
        void 실패() throws Exception {
            // given
            given(adminManageService.getAdmin(adminId)).willThrow(new CustomException(ErrorCode.ADMIN_NOT_FOUND));

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/admin/admin-management/{adminId}", adminId)
                .with(csrf().asHeader()));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.ADMIN_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.ADMIN_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.status").value(ErrorCode.ADMIN_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.name").value(ErrorCode.ADMIN_NOT_FOUND.name()));

            actions
                .andDo(document("admin-management-detail-fail",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("adminId").description("관리자 ID")
                    )
                ));

            then(adminManageService).should().getAdmin(adminId);
        }
    }

    @Nested
    @DisplayName("관리자 저장")
    class 관리자_저장 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            AdminAddReqDto adminAddReqDto = AdminAddReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm(password)
                .build();

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/admin-management")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminAddReqDto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("관리자 등록 성공"));

            actions
                .andDo(document("admin-management-add",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            then(adminManageService).should().addAdmin(any());
            then(adminLogService).should().addAdminLog(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 비밀번호와 비밀번호 확인이 다른 경우")
        void 실패_비밀번호화_비밀번호_확인이_다른_경우() throws Exception {
            // given
            AdminAddReqDto adminAddReqDto = AdminAddReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm("differentPassword")
                .build();
            willThrow(new CustomException(ErrorCode.MISMATCH_PASSWORD_AND_PASSWORD_CONFIRM)).given(adminManageService).addAdmin(any(AdminAddReqDto.class));

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/admin-management")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminAddReqDto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.MISMATCH_PASSWORD_AND_PASSWORD_CONFIRM.getMessage()));

            actions
                .andDo(document("admin-management-add-fail",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            then(adminManageService).should().addAdmin(any(AdminAddReqDto.class));
        }

        @Test
        @DisplayName("실패 - 중복된 adminId를 입력한 경우")
        void 실패_존재하는_adminId() throws Exception {
            // given
            AdminAddReqDto adminAddReqDto = AdminAddReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm(password)
                .build();
            willThrow(new CustomException(ErrorCode.EXIST_USER_ID)).given(adminManageService).addAdmin(any(AdminAddReqDto.class));

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/admin-management")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminAddReqDto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.EXIST_USER_ID.getMessage()));

            actions
                .andDo(document("admin-management-add-fail",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            then(adminManageService).should().addAdmin(any(AdminAddReqDto.class));
        }
    }

    @Nested
    @DisplayName("관리자 수정")
    class 관리자_수정 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            AdminModifyReqDto adminModifyReqDto = AdminModifyReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm(password)
                .build();

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/admin/admin-management")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminModifyReqDto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("관리자 수정 성공"));

            actions
                .andDo(document("admin-management-modify",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            then(adminManageService).should().modifyAdmin(any());
            then(adminLogService).should().addAdminLog(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 adminId를 입력한 경우")
        void 실패_존재하지_않는_adminId() throws Exception {
            // given
            AdminModifyReqDto adminModifyReqDto = AdminModifyReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm(password)
                .build();
            willThrow(new CustomException(ErrorCode.ADMIN_NOT_FOUND)).given(adminManageService).modifyAdmin(any(AdminModifyReqDto.class));

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/admin/admin-management")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminModifyReqDto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.ADMIN_NOT_FOUND.getMessage()));

            actions
                .andDo(document("admin-management-modify-fail-incorrect-adminId",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            then(adminManageService).should().modifyAdmin(any(AdminModifyReqDto.class));
        }

        @Test
        @DisplayName("실패 - 비밀번호와 비밀번호 확인이 다른 경우")
        void 실패_비밀번호와_비밀번호_확인이_다른_경우() throws Exception {
            // given
            AdminModifyReqDto adminModifyReqDto = AdminModifyReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm("differentPassword")
                .build();
            willThrow(new CustomException(ErrorCode.MISMATCH_PASSWORD_AND_PASSWORD_CONFIRM)).given(adminManageService).modifyAdmin(any(AdminModifyReqDto.class));

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/admin/admin-management")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminModifyReqDto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.MISMATCH_PASSWORD_AND_PASSWORD_CONFIRM.getMessage()));

            actions
                .andDo(document("admin-management-modify-fail-incorrect-password",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            then(adminManageService).should().modifyAdmin(any(AdminModifyReqDto.class));
        }

        @Test
        @DisplayName("실패 - 비밀번호의 길이가 적절하지 않는 경우")
        void 실패_비밀번호의_길이가_적절하지_않는_경우() throws Exception {
            // given
            AdminModifyReqDto adminModifyReqDto = AdminModifyReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password("short")
                .passwordConfirm("short")
                .build();
            willThrow(new CustomException(ErrorCode.INCORRECT_PASSWORD_LENGTH)).given(adminManageService).modifyAdmin(any(AdminModifyReqDto.class));

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/admin/admin-management")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(adminModifyReqDto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_LENGTH.getMessage()));

            actions
                .andDo(document("admin-management-modify-fail-incorrect-password-length",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            then(adminManageService).should().modifyAdmin(any(AdminModifyReqDto.class));
        }
    }

    @Nested
    @DisplayName("관리자 삭제")
    class 관리자_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/admin/admin-management/{adminId}", adminId)
                .with(csrf().asHeader()));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("관리자 삭제 성공"));

            actions
                .andDo(document("admin-management-remove",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("adminId").description("관리자 ID")
                    )
                ));

            then(adminManageService).should().removeAdmin(adminId);
            then(adminLogService).should().addAdminLog(any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 자신의 계정을 삭제한 경우")
        void 실패_자신의_계정을_삭제한_경우() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.SELF_DELETION_NOT_ALLOWED)).given(adminManageService).removeAdmin(adminId);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/admin/admin-management/{adminId}", adminId)
                .with(csrf().asHeader()));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.SELF_DELETION_NOT_ALLOWED.getMessage()));

            actions
                .andDo(document("admin-management-remove-fail",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("adminId").description("관리자 ID")
                    )
                ));

            then(adminManageService).should().removeAdmin(adminId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 adminId를 입력한 경우")
        void 실패_존재하지_않는_adminId() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.ADMIN_NOT_FOUND)).given(adminManageService).removeAdmin(adminId);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/admin/admin-management/{adminId}", adminId)
                .with(csrf().asHeader()));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.ADMIN_NOT_FOUND.getMessage()));

            actions
                .andDo(document("admin-management-remove-fail",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("adminId").description("관리자 ID")
                    )
                ));

            then(adminManageService).should().removeAdmin(adminId);
        }
    }

    @Nested
    @DisplayName("관리자 아이디 중복 체크")
    class 관리자_아이디_중복_체크 {

        @Test
        @DisplayName("성공 - 중복된 아이디가 없는 경우")
        void 성공() throws Exception {
            // given
            given(adminManageService.checkDuplicateAdminId(adminId)).willReturn(false);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/admin/admin-management/check-duplicate/{adminId}", adminId)
                .with(csrf().asHeader()));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));

            actions
                .andDo(document("admin-management-check-duplicate-id-no",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("adminId").description("관리자 ID")
                    )
                ));

            then(adminManageService).should().checkDuplicateAdminId(adminId);
        }

        @Test
        @DisplayName("성공 - 중복된 아이디가 있는 경우")
        void 성공_중복된_아이디가_있는_경우() throws Exception {
            // given
            given(adminManageService.checkDuplicateAdminId(adminId)).willReturn(true);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/admin/admin-management/check-duplicate/{adminId}", adminId)
                .with(csrf().asHeader()));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

            actions
                .andDo(document("admin-management-check-duplicate-id-ok",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("adminId").description("관리자 ID")
                    )
                ));

            then(adminManageService).should().checkDuplicateAdminId(adminId);
        }
    }
}