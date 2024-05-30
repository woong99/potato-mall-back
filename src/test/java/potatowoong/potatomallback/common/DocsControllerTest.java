package potatowoong.potatomallback.common;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.customResponseFields;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.config.security.PortCheckFilter;

@WebMvcTest(controllers = DocsController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PortCheckFilter.class)})
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class DocsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("공통 성공 응답")
    void 공통_성공() throws Exception {
        // when & then
        ResultActions actions = mockMvc.perform(get("/api/docs/ok")
            .with(csrf().asHeader()));

        actions
            .andExpect(status().isOk());

        actions
            .andDo(document("docs-ok",
                    customResponseFields("custom-response", null,
                        attributes(key("title").value("공통 성공 응답")),
                        fieldWithPath("status").description("응답 상태"),
                        subsectionWithPath("data").description("응답 데이터"))
                )
            );
    }

    @Test
    @DisplayName("공통 에러 응답")
    void 공통_에러() throws Exception {
        // when & then
        ResultActions actions = mockMvc.perform(get("/api/docs/error")
            .with(csrf().asHeader()));

        actions
            .andExpect(status().isBadRequest());

        actions
            .andDo(document("docs-error",
                    customResponseFields("custom-response", null,
                        attributes(key("title").value("공통 실패 응답")),
                        fieldWithPath("status").description("응답 상태"),
                        fieldWithPath("name").description("에러명"),
                        fieldWithPath("code").description("에러 코드"),
                        fieldWithPath("message").description("에러 메시지")
                    )
                )
            );
    }
}