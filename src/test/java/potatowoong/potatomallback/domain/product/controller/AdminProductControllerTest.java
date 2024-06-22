package potatowoong.potatomallback.domain.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentRequest;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;
import static potatowoong.potatomallback.global.common.LogMessage.ADD;
import static potatowoong.potatomallback.global.common.LogMessage.MODIFY;
import static potatowoong.potatomallback.global.common.LogMessage.PRODUCT_MANAGEMENT;
import static potatowoong.potatomallback.global.common.LogMessage.REMOVE;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_DETAIL;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_LIST;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.auth.service.AdminLogService;
import potatowoong.potatomallback.domain.product.dto.request.ProductReqDto.ProductAddReqDto;
import potatowoong.potatomallback.domain.product.dto.request.ProductReqDto.ProductModifyReqDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductDetailResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductSearchResDto;
import potatowoong.potatomallback.domain.product.service.ProductService;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@WebMvcTest(controllers = AdminProductController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class AdminProductControllerTest {

    @MockBean
    private ProductService productService;

    @MockBean
    private AdminLogService adminLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final long productId = 1L;

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
                .searchCondition("name")
                .sortCondition("price")
                .sortDirection(SortDirection.DESCENDING)
                .build();

            ProductSearchResDto resDto = getProductSearchResDto();
            PageResponseDto<ProductSearchResDto> pageResponseDto = new PageResponseDto<>(List.of(resDto), productId);

            given(productService.getProductList(pageRequestDto)).willReturn(pageResponseDto);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/admin/product/search")
                .with(csrf().asHeader())
                .param("page", "0")
                .param("size", "10")
                .param("searchWord", "감자")
                .param("searchCondition", "name")
                .param("sortCondition", "price")
                .param("sortDirection", "DESCENDING")
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").exists())
                .andExpect(jsonPath("$.data.totalElements").value(1));

            actions
                .andDo(document("product-search",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    queryParameters(
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기"),
                        parameterWithName("searchWord").description("검색어"),
                        parameterWithName("searchCondition").description("검색 조건(name: 상품명)"),
                        parameterWithName("sortCondition").description("정렬 조건(name: 상품명, price: 가격, categoryName: 카테고리명, stockCount: 재고량)"),
                        parameterWithName("sortDirection").description("정렬 방향(DESCENDING: 내림차순, ASCENDING: 오름차순)")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("result[].productId").description("상품 ID"),
                        fieldWithPath("result[].name").description("상품명"),
                        fieldWithPath("result[].price").description("가격"),
                        fieldWithPath("result[].stockQuantity").description("재고량"),
                        fieldWithPath("result[].categoryName").description("카테고리명"),
                        fieldWithPath("result[].thumbnailUrl").description("썸네일 URL"),
                        fieldWithPath("result[].updatedAt").description("수정일"),
                        fieldWithPath("totalElements").description("전체 상품 수")
                    )
                ));

            then(productService).should().getProductList(pageRequestDto);
            then(adminLogService).should().addAdminLog(PRODUCT_MANAGEMENT, SEARCH_LIST);
        }

        private ProductSearchResDto getProductSearchResDto() {
            return ProductSearchResDto.builder()
                .productId(productId)
                .name("감자")
                .price(1000)
                .stockQuantity(10)
                .categoryName("채소")
                .thumbnailUrl("https://xxx.s3.xxx.amazonaws.com/xxx/xxxx.jpg")
                .updatedAt(LocalDateTime.now())
                .build();
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class 상품_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            ProductDetailResDto resDto = getProductDetailResDto();

            given(productService.getProduct(productId)).willReturn(resDto);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/admin/product/{productId}", productId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.name").value("감자"))
                .andExpect(jsonPath("$.data.description").value("감자입니다."))
                .andExpect(jsonPath("$.data.price").value(1000))
                .andExpect(jsonPath("$.data.stockQuantity").value(10))
                .andExpect(jsonPath("$.data.productCategoryId").value(1L))
                .andExpect(jsonPath("$.data.thumbnailUrl").value("https://xxx.s3.xxx.amazonaws.com/xxx/xxxx.jpg"))
                .andExpect(jsonPath("$.data.thumbnailFileId").value(1L));

            actions
                .andDo(document("product-detail",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("productId").description("상품 ID")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("productId").description("상품 ID"),
                        fieldWithPath("name").description("상품명"),
                        fieldWithPath("description").description("상품 설명"),
                        fieldWithPath("price").description("가격"),
                        fieldWithPath("stockQuantity").description("재고량"),
                        fieldWithPath("productCategoryId").description("상품 카테고리 ID"),
                        fieldWithPath("thumbnailUrl").description("썸네일 URL"),
                        fieldWithPath("thumbnailFileId").description("썸네일 파일 ID")
                    )
                ));

            then(productService).should().getProduct(productId);
            then(adminLogService).should().addAdminLog(PRODUCT_MANAGEMENT, SEARCH_DETAIL, productId, resDto.name());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품 ID")
        void 실패_존재하지_않는_상품_ID() throws Exception {
            // given
            given(productService.getProduct(productId)).willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/admin/product/{productId}", productId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));

            actions
                .andDo(document("product-detail-fail-not-found-product",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("productId").description("상품 ID")
                    )
                ));

            then(productService).should().getProduct(productId);
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }

        private ProductDetailResDto getProductDetailResDto() {
            return ProductDetailResDto.builder()
                .productId(1L)
                .name("감자")
                .description("감자입니다.")
                .price(1000)
                .stockQuantity(10)
                .productCategoryId(1L)
                .thumbnailUrl("https://xxx.s3.xxx.amazonaws.com/xxx/xxxx.jpg")
                .thumbnailFileId(1L)
                .build();
        }
    }

    @Nested
    @DisplayName("상품 등록")
    class 상품_등록 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            ProductAddReqDto productAddReqDto = getProductAddReqDto();

            MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnailFile", "test.png", "image/png", "썸네일 파일".getBytes());
            MockMultipartFile data = new MockMultipartFile("productAddReqDto", null, MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(productAddReqDto).getBytes());

            // when & then
            ResultActions actions = mockMvc.perform(multipart(HttpMethod.POST, "/api/admin/product")
                .file(thumbnailFile)
                .file(data)
                .with(csrf().asHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("상품 등록 성공"));

            actions
                .andDo(document("product-add",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestPartFields(
                        "productAddReqDto",
                        fieldWithPath("name").optional().description("상품명"),
                        fieldWithPath("content").optional().description("상품 설명"),
                        fieldWithPath("price").optional().description("가격"),
                        fieldWithPath("stockQuantity").optional().description("재고량"),
                        fieldWithPath("productCategoryId").optional().description("상품 카테고리 ID")
                    )
                ));

            then(productService).should().addProduct(any(), any());
            then(adminLogService).should().addAdminLog(PRODUCT_MANAGEMENT, ADD, "", productAddReqDto.name());
        }

        @Test
        @DisplayName("실패 - 상품명 중복")
        void 실패_상품명_중복() throws Exception {
            // given
            ProductAddReqDto productAddReqDto = getProductAddReqDto();

            MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnailFile", "test.png", "image/png", "썸네일 파일".getBytes());
            MockMultipartFile data = new MockMultipartFile("productAddReqDto", null, MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(productAddReqDto).getBytes());

            willThrow(new CustomException(ErrorCode.DUPLICATED_PRODUCT_NAME)).given(productService).addProduct(any(), any());

            // when & then
            ResultActions actions = mockMvc.perform(multipart(HttpMethod.POST, "/api/admin/product")
                .file(thumbnailFile)
                .file(data)
                .with(csrf().asHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATED_PRODUCT_NAME.getMessage()));

            actions
                .andDo(document("product-add-fail-duplicated-product-name",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestPartFields(
                        "productAddReqDto",
                        fieldWithPath("name").optional().description("상품명"),
                        fieldWithPath("content").optional().description("상품 설명"),
                        fieldWithPath("price").optional().description("가격"),
                        fieldWithPath("stockQuantity").optional().description("재고량"),
                        fieldWithPath("productCategoryId").optional().description("상품 카테고리 ID")
                    )
                ));

            then(productService).should().addProduct(any(), any());
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품 카테고리 ID")
        void 실패_존재하지_않는_상품_카테고리_ID() throws Exception {
            // given
            ProductAddReqDto productAddReqDto = getProductAddReqDto();

            MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnailFile", "test.png", "image/png", "썸네일 파일".getBytes());
            MockMultipartFile data = new MockMultipartFile("productAddReqDto", null, MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(productAddReqDto).getBytes());

            willThrow(new CustomException(ErrorCode.NOT_FOUND_CATEGORY)).given(productService).addProduct(any(), any());

            // when & then
            ResultActions actions = mockMvc.perform(multipart(HttpMethod.POST, "/api/admin/product")
                .file(thumbnailFile)
                .file(data)
                .with(csrf().asHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_FOUND_CATEGORY.getMessage()));

            actions
                .andDo(document("product-add-fail-not-found-category",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestPartFields(
                        "productAddReqDto",
                        fieldWithPath("name").optional().description("상품명"),
                        fieldWithPath("content").optional().description("상품 설명"),
                        fieldWithPath("price").optional().description("가격"),
                        fieldWithPath("stockQuantity").optional().description("재고량"),
                        fieldWithPath("productCategoryId").optional().description("상품 카테고리 ID")
                    )
                ));

            then(productService).should().addProduct(any(), any());
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }

        private ProductAddReqDto getProductAddReqDto() {
            return ProductAddReqDto.builder()
                .name("감자")
                .content("감자입니다.")
                .price(1000)
                .stockQuantity(10)
                .productCategoryId(1L)
                .build();
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class 상품_수정 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            ProductModifyReqDto productModifyReqDto = getProductModifyReqDto();

            MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnailFile", "test.png", "image/png", "test".getBytes());
            MockMultipartFile data = new MockMultipartFile("productModifyReqDto", null, MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(productModifyReqDto).getBytes());

            // when & then
            ResultActions actions = mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/product")
                .file(thumbnailFile)
                .file(data)
                .with(csrf().asHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("상품 수정 성공"));

            actions
                .andDo(document("product-modify",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestPartFields(
                        "productModifyReqDto",
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("name").optional().description("상품명"),
                        fieldWithPath("content").optional().description("상품 설명"),
                        fieldWithPath("price").optional().description("가격"),
                        fieldWithPath("stockQuantity").optional().description("재고량"),
                        fieldWithPath("productCategoryId").optional().description("상품 카테고리 ID"),
                        fieldWithPath("thumbnailFileId").description("썸네일 파일 ID")
                    )
                ));

            then(productService).should().modifyProduct(any(), any());
            then(adminLogService).should().addAdminLog(PRODUCT_MANAGEMENT, MODIFY, productId, productModifyReqDto.name());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품 ID")
        void 실패_존재하지_않는_상품_ID() throws Exception {
            // given
            ProductModifyReqDto productModifyReqDto = getProductModifyReqDto();

            MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnailFile", "test.png", "image/png", "test".getBytes());
            MockMultipartFile data = new MockMultipartFile("productModifyReqDto", null, MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(productModifyReqDto).getBytes());

            willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND)).given(productService).modifyProduct(any(), any());

            // when & then
            ResultActions actions = mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/product")
                .file(thumbnailFile)
                .file(data)
                .with(csrf().asHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));

            actions
                .andDo(document("product-modify-fail-not-found-product",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestPartFields(
                        "productModifyReqDto",
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("name").optional().description("상품명"),
                        fieldWithPath("content").optional().description("상품 설명"),
                        fieldWithPath("price").optional().description("가격"),
                        fieldWithPath("stockQuantity").optional().description("재고량"),
                        fieldWithPath("productCategoryId").optional().description("상품 카테고리 ID"),
                        fieldWithPath("thumbnailFileId").description("썸네일 파일 ID")
                    )
                ));

            then(productService).should().modifyProduct(any(), any());
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 상품명 중복")
        void 실패_상품명_중복() throws Exception {
            // given
            ProductModifyReqDto productModifyReqDto = getProductModifyReqDto();

            MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnailFile", "test.png", "image/png", "test".getBytes());
            MockMultipartFile data = new MockMultipartFile("productModifyReqDto", null, MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(productModifyReqDto).getBytes());

            willThrow(new CustomException(ErrorCode.DUPLICATED_PRODUCT_NAME)).given(productService).modifyProduct(any(), any());

            // when & then
            ResultActions actions = mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/product")
                .file(thumbnailFile)
                .file(data)
                .with(csrf().asHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATED_PRODUCT_NAME.getMessage()));

            actions
                .andDo(document("product-modify-fail-duplicated-product-name",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestPartFields(
                        "productModifyReqDto",
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("name").optional().description("상품명"),
                        fieldWithPath("content").optional().description("상품 설명"),
                        fieldWithPath("price").optional().description("가격"),
                        fieldWithPath("stockQuantity").optional().description("재고량"),
                        fieldWithPath("productCategoryId").optional().description("상품 카테고리 ID"),
                        fieldWithPath("thumbnailFileId").description("썸네일 파일 ID")
                    )
                ));

            then(productService).should().modifyProduct(any(), any());
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품 카테고리 ID")
        void 실패_존재하지_않는_상품_카테고리_ID() throws Exception {
            // given
            ProductModifyReqDto productModifyReqDto = getProductModifyReqDto();

            MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnailFile", "test.png", "image/png", "test".getBytes());
            MockMultipartFile data = new MockMultipartFile("productModifyReqDto", null, MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(productModifyReqDto).getBytes());

            willThrow(new CustomException(ErrorCode.NOT_FOUND_CATEGORY)).given(productService).modifyProduct(any(), any());

            // when & then
            ResultActions actions = mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/product")
                .file(thumbnailFile)
                .file(data)
                .with(csrf().asHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_FOUND_CATEGORY.getMessage()));

            actions
                .andDo(document("product-modify-fail-not-found-category",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestPartFields(
                        "productModifyReqDto",
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("name").optional().description("상품명"),
                        fieldWithPath("content").optional().description("상품 설명"),
                        fieldWithPath("price").optional().description("가격"),
                        fieldWithPath("stockQuantity").optional().description("재고량"),
                        fieldWithPath("productCategoryId").optional().description("상품 카테고리 ID"),
                        fieldWithPath("thumbnailFileId").description("썸네일 파일 ID")
                    )
                ));

            then(productService).should().modifyProduct(any(), any());
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }

        private ProductModifyReqDto getProductModifyReqDto() {
            return ProductModifyReqDto.builder()
                .productId(productId)
                .name("감자")
                .content("감자입니다.")
                .price(1000)
                .stockQuantity(10)
                .productCategoryId(1L)
                .thumbnailFileId(1L)
                .build();
        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class 상품_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/admin/product/{productId}", productId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("상품 삭제 성공"));

            actions
                .andDo(document("product-remove",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("productId").description("상품 ID")
                    )
                ));

            then(productService).should().removeProduct(productId);
            then(adminLogService).should().addAdminLog(PRODUCT_MANAGEMENT, REMOVE, productId, "");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품 ID")
        void 실패_존재하지_않는_상품_ID() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND)).given(productService).removeProduct(productId);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/admin/product/{productId}", productId)
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));

            actions
                .andDo(document("product-remove-fail-not-found-product",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("productId").description("상품 ID")
                    )
                ));

            then(productService).should().removeProduct(productId);
            then(adminLogService).should(never()).addAdminLog(any(), any(), any(), any());
        }
    }
}