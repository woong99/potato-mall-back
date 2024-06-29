package potatowoong.potatomallback.domain.product.controller;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.product.dto.response.ProductNameResDto;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto;
import potatowoong.potatomallback.domain.product.service.ProductSearchService;
import potatowoong.potatomallback.domain.product.service.UserProductService;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;


@WebMvcTest(controllers = UserProductController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class UserProductControllerTest {

    @MockBean
    private UserProductService userProductService;

    @MockBean
    private ProductSearchService productSearchService;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("상품 목록 조회")
    class 상품_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            UserProductResDto.Search resDto = UserProductResDto.Search.builder()
                .productId(1L)
                .name("감자")
                .price(1000)
                .thumbnailUrl("http://localhost:8080/api/file/download/1")
                .build();
            PageResponseDto<UserProductResDto.Search> pageResponseDto = new PageResponseDto<>(Collections.singletonList(resDto), 1L);

            given(userProductService.getUserProductList(any())).willReturn(pageResponseDto);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/product/search")
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
                        fieldWithPath("result[].likeCount").description("좋아요 개수"),
                        fieldWithPath("result[].isLike").description("좋아요 여부"),
                        fieldWithPath("result[].reviewCount").description("리뷰 개수"),
                        fieldWithPath("totalElements").description("전체 개수")
                    )
                ));

            then(userProductService).should().getUserProductList(any());
        }
    }

    @Nested
    @DisplayName("상품명 검색(자동완성)")
    class 상품명_검색_자동완성 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            String searchWord = "감";

            ProductNameResDto productNameResDto = ProductNameResDto.builder()
                .name("감자")
                .build();

            given(productSearchService.searchProductNameWithAutoComplete(searchWord)).willReturn(Collections.singletonList(productNameResDto));

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/product/search-with-auto-complete")
                .with(csrf().asHeader())
                .param("searchWord", searchWord)
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

            actions
                .andDo(document("user-product-search-with-auto-complete",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    queryParameters(
                        parameterWithName("searchWord").description("검색어(상품명)")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("name").description("상품명")
                    )
                ));

            then(productSearchService).should().searchProductNameWithAutoComplete(searchWord);
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class 상품_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            long productId = 1L;

            UserProductResDto.Detail resDto = UserProductResDto.Detail.builder()
                .productId(1L)
                .name("감자")
                .description("감자입니다.")
                .price(1000)
                .stockQuantity(10)
                .thumbnailUrl("http://localhost:8080/api/file/download/1")
                .likeCount(0)
                .isLike(false)
                .build();

            given(userProductService.getUserProduct(productId)).willReturn(resDto);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/user/product/{productId}", productId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

            actions
                .andDo(document("user-product-detail",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("productId").description("상품 ID"),
                        fieldWithPath("name").description("상품명"),
                        fieldWithPath("description").description("상품 설명"),
                        fieldWithPath("price").description("가격"),
                        fieldWithPath("stockQuantity").description("재고 수량"),
                        fieldWithPath("thumbnailUrl").description("썸네일 URL"),
                        fieldWithPath("likeCount").description("좋아요 개수"),
                        fieldWithPath("isLike").description("좋아요 여부")
                    )
                ));

            then(userProductService).should().getUserProduct(productId);
        }

        @Test
        @DisplayName("실패 - 상품이 존재하지 않음")
        void 실패_상품이_존재하지_않음() throws Exception {
            // given
            long productId = 1L;

            given(userProductService.getUserProduct(productId)).willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/user/product/{productId}", productId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isBadRequest());

            actions
                .andDo(document("user-product-detail-fail",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse()
                ));

            then(userProductService).should().getUserProduct(productId);
        }
    }

}