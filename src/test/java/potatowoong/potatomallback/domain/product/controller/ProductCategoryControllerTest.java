package potatowoong.potatomallback.domain.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
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
import static potatowoong.potatomallback.global.common.LogMessage.ADD;
import static potatowoong.potatomallback.global.common.LogMessage.MODIFY;
import static potatowoong.potatomallback.global.common.LogMessage.PRODUCT_CATEGORY_MANAGEMENT;
import static potatowoong.potatomallback.global.common.LogMessage.REMOVE;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_DETAIL;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_LIST;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.auth.service.AdminLogService;
import potatowoong.potatomallback.domain.product.dto.request.ProductCategoryReqDto.ProductCategoryAddReqDto;
import potatowoong.potatomallback.domain.product.dto.request.ProductCategoryReqDto.ProductCategoryModifyReqDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategoryDetailResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategoryListResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategorySearchResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductRelatedResDto;
import potatowoong.potatomallback.domain.product.service.ProductCategoryService;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.common.ResponseText;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@WebMvcTest(controllers = ProductCategoryController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class ProductCategoryControllerTest {

    @MockBean
    private ProductCategoryService productCategoryService;

    @MockBean
    private AdminLogService adminLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final String categoryName = "카테고리명";

    private final long categoryId = 1L;

    @Nested
    @DisplayName("상품 카테고리 목록 조회")
    class 상품_카테고리_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            ProductCategorySearchResDto resDto = ProductCategorySearchResDto.builder()
                .productCategoryId(categoryId)
                .name(categoryName)
                .productCount(10)
                .updatedAt(LocalDateTime.now())
                .build();
            PageResponseDto<ProductCategorySearchResDto> pageResponseDto = new PageResponseDto<>(List.of(resDto), 1);

            given(productCategoryService.getProductCategoryList(any())).willReturn(pageResponseDto);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/admin/product-category/search")
                .with(csrf().asHeader())
                .param("page", "0")
                .param("size", "10")
                .param("searchWord", "감자")
                .param("searchCondition", "name")
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").exists())
                .andExpect(jsonPath("$.data.totalElements").value(1));

            actions
                .andDo(document("product-category-search",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    queryParameters(
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기"),
                        parameterWithName("searchWord").description("검색어"),
                        parameterWithName("searchCondition").description("검색 조건(name : 카테고리명)")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("result[].productCategoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                        fieldWithPath("result[].name").type(JsonFieldType.STRING).description("카테고리명"),
                        fieldWithPath("result[].updatedAt").type(JsonFieldType.STRING).description("최종 수정 일시"),
                        fieldWithPath("result[].productCount").type(JsonFieldType.NUMBER).description("등록된 상품 수"),
                        fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("전체 상품 카테고리 수")
                    )
                ));

            then(productCategoryService).should().getProductCategoryList(any());
            then(adminLogService).should().addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, SEARCH_LIST);
        }
    }

    @Nested
    @DisplayName("상품 카테고리 상세 조회")
    class 상품_카테고리_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            ProductRelatedResDto productRelatedResDto = ProductRelatedResDto.builder()
                .productId(1L)
                .name("상품명")
                .price(1000)
                .build();
            ProductCategoryDetailResDto resDto = ProductCategoryDetailResDto.builder()
                .productCategoryId(categoryId)
                .name(categoryName)
                .products(List.of(productRelatedResDto))
                .build();

            given(productCategoryService.getProductCategory(categoryId)).willReturn(resDto);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/admin/product-category/{id}", categoryId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productCategoryId").value(categoryId))
                .andExpect(jsonPath("$.data.name").value(categoryName))
                .andExpect(jsonPath("$.data.products[0].productId").value(1L))
                .andExpect(jsonPath("$.data.products[0].name").value("상품명"))
                .andExpect(jsonPath("$.data.products[0].price").value(1000));

            actions
                .andDo(document("product-category-detail",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("id").description("카테고리 ID")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("productCategoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description(categoryName),
                        fieldWithPath("products[].productId").type(JsonFieldType.NUMBER).description("상품 ID"),
                        fieldWithPath("products[].name").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("products[].price").type(JsonFieldType.NUMBER).description("상품 가격")
                    )
                ));

            then(productCategoryService).should().getProductCategory(categoryId);
            then(adminLogService).should().addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, SEARCH_DETAIL, categoryId, resDto.name());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리 ID")
        void 실패_존재하지_않는_카테고리_ID() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.NOT_FOUND_CATEGORY)).given(productCategoryService).getProductCategory(categoryId);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/admin/product-category/{id}", categoryId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_FOUND_CATEGORY.getMessage()));

            actions
                .andDo(document("product-category-detail-fail-not-found-category",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("id").description("카테고리 ID")
                    )
                ));

            then(productCategoryService).should().getProductCategory(categoryId);
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("상품 카테고리 전체 조회")
    class 상품_카테고리_전체_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            ProductCategoryListResDto resDto = ProductCategoryListResDto.builder()
                .productCategoryId(categoryId)
                .name(categoryName)
                .build();

            given(productCategoryService.getAllProductCategoryList()).willReturn(List.of(resDto));

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/admin/product-category/all")
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

            actions
                .andDo(document("product-category-all",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("productCategoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("카테고리명")
                    )
                ));

            then(productCategoryService).should().getAllProductCategoryList();
        }
    }

    @Nested
    @DisplayName("상품 카테고리 등록")
    class 상품_카테고리_등록 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            ProductCategoryAddReqDto dto = ProductCategoryAddReqDto.builder()
                .name(categoryName)
                .build();

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/product-category")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_ADD_PRODUCT_CATEGORY));

            actions
                .andDo(document("product-category-add",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("name").type(JsonFieldType.STRING).optional().description(categoryName)
                    )
                ));

            then(productCategoryService).should().addProductCategory(dto);
            then(adminLogService).should().addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, ADD, "", categoryName);
        }

        @Test
        @DisplayName("실패 - 중복된 카테고리명")
        void 실패_중복된_카테고리명() throws Exception {
            // given
            ProductCategoryAddReqDto dto = ProductCategoryAddReqDto.builder()
                .name(categoryName)
                .build();

            willThrow(new CustomException(ErrorCode.DUPLICATED_CATEGORY_NAME)).given(productCategoryService).addProductCategory(dto);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/admin/product-category")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATED_CATEGORY_NAME.getMessage()));

            actions
                .andDo(document("product-category-add-fail-duplicated-category-name",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("name").type(JsonFieldType.STRING).optional().description(categoryName)
                    )
                ));

            then(productCategoryService).should().addProductCategory(dto);
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("상품 카테고리 수정")
    class 상품_카테고리_수정 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            ProductCategoryModifyReqDto dto = ProductCategoryModifyReqDto.builder()
                .productCategoryId(categoryId)
                .name(categoryName)
                .build();

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/admin/product-category")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_MODIFY_PRODUCT_CATEGORY));

            actions
                .andDo(document("product-category-modify",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("productCategoryId").type(JsonFieldType.NUMBER).optional().description("카테고리 ID"),
                        fieldWithPath("name").type(JsonFieldType.STRING).optional().description(categoryName)
                    )
                ));

            then(productCategoryService).should().modifyProductCategory(dto);
            then(adminLogService).should().addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, MODIFY, dto.productCategoryId(), dto.name());
        }

        @Test
        @DisplayName("실패 - 중복된 카테고리명")
        void 실패_중복된_카테고리명() throws Exception {
            // given
            ProductCategoryModifyReqDto dto = ProductCategoryModifyReqDto.builder()
                .productCategoryId(categoryId)
                .name(categoryName)
                .build();

            willThrow(new CustomException(ErrorCode.DUPLICATED_CATEGORY_NAME)).given(productCategoryService).modifyProductCategory(dto);

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/admin/product-category")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATED_CATEGORY_NAME.getMessage()));

            actions
                .andDo(document("product-category-modify-fail-duplicated-category-name",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("productCategoryId").type(JsonFieldType.NUMBER).optional().description("카테고리 ID"),
                        fieldWithPath("name").type(JsonFieldType.STRING).optional().description(categoryName)
                    )
                ));

            then(productCategoryService).should().modifyProductCategory(dto);
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리 ID")
        void 실패_존재하지_않는_카테고리_ID() throws Exception {
            // given
            ProductCategoryModifyReqDto dto = ProductCategoryModifyReqDto.builder()
                .productCategoryId(categoryId)
                .name(categoryName)
                .build();

            willThrow(new CustomException(ErrorCode.NOT_FOUND_CATEGORY)).given(productCategoryService).modifyProductCategory(dto);

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/admin/product-category")
                .with(csrf().asHeader())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_FOUND_CATEGORY.getMessage()));

            actions
                .andDo(document("product-category-modify-fail-not-found-category",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("productCategoryId").type(JsonFieldType.NUMBER).optional().description("카테고리 ID"),
                        fieldWithPath("name").type(JsonFieldType.STRING).optional().description(categoryName)
                    )
                ));

            then(productCategoryService).should().modifyProductCategory(dto);
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("상품 카테고리 삭제")
    class 상품_카테고리_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            given(productCategoryService.getProductCategoryName(categoryId)).willReturn(categoryName);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/admin/product-category/{id}", categoryId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_REMOVE_PRODUCT_CATEGORY));

            actions
                .andDo(document("product-category-remove",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("id").description("카테고리 ID")
                    )
                ));

            then(productCategoryService).should().getProductCategoryName(categoryId);
            then(productCategoryService).should().removeProductCategory(categoryId);
            then(adminLogService).should().addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, REMOVE, categoryId, categoryName);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리 ID")
        void 실패_존재하지_않는_카테고리_ID() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.NOT_FOUND_CATEGORY)).given(productCategoryService).removeProductCategory(categoryId);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/admin/product-category/{id}", categoryId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_FOUND_CATEGORY.getMessage()));

            actions
                .andDo(document("product-category-remove-fail-not-found-category",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("id").description("카테고리 ID")
                    )
                ));

            then(productCategoryService).should().removeProductCategory(categoryId);
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 카테고리에 속한 상품이 존재하는 경우")
        void 실패_카테고리에_속한_상품이_존재하는_경우() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.EXIST_PRODUCT_IN_CATEGORY)).given(productCategoryService).removeProductCategory(categoryId);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/admin/product-category/{id}", categoryId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.EXIST_PRODUCT_IN_CATEGORY.getMessage()));

            actions
                .andDo(document("product-category-remove-fail-exist-product-in-category",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("id").description("카테고리 ID")
                    )
                ));

            then(productCategoryService).should().removeProductCategory(categoryId);
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }
    }
}