package potatowoong.potatomallback.product.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getNoAuthDocumentRequest;

import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.product.dto.response.ProductResDto.UserProductSearchResDto;
import potatowoong.potatomallback.product.service.ProductService;


@WebMvcTest(controllers = UserProductController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class UserProductControllerTest {

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("상품 목록 조회")
    class 상품_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            PageRequestDto pageRequestDto = PageRequestDto.builder()
                .page(0)
                .size(10)
                .searchWord("감자")
                .sortCondition("lowPrice")
                .build();

            UserProductSearchResDto resDto = UserProductSearchResDto.builder()
                .productId(1L)
                .name("감자")
                .price(1000)
                .thumbnailUrl("http://localhost:8080/api/file/download/1")
                .build();
            PageResponseDto<UserProductSearchResDto> pageResponseDto = new PageResponseDto<>(Collections.singletonList(resDto), 1L);

            given(productService.getUserProductList(pageRequestDto)).willReturn(pageResponseDto);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/product/search")
                .with(csrf().asHeader())
                .param("page", "0")
                .param("size", "10")
                .param("searchWord", "감자")
                .param("sortCondition", "lowPrice")
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").exists())
                .andExpect(jsonPath("$.data.totalElements").value(1));

            actions
                .andDo(document("user-product-search",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    queryParameters(
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기"),
                        parameterWithName("searchWord").description("검색어(상품명)"),
                        parameterWithName("sortCondition").description("정렬 조건(lowPrice: 낮은가격순, highPrice: 높은가격순, latest: 최신순)") // TODO : 판매량순 추가
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("result[].productId").description("상품 ID"),
                        fieldWithPath("result[].name").description("상품명"),
                        fieldWithPath("result[].price").description("가격"),
                        fieldWithPath("result[].thumbnailUrl").description("썸네일 URL"),
                        fieldWithPath("totalElements").description("전체 개수")
                    )
                ));

            then(productService).should().getUserProductList(pageRequestDto);
        }
    }
}