package potatowoong.potatomallback.domain.product.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentRequest;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;

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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.product.dto.response.UserProductLikeResDto;
import potatowoong.potatomallback.domain.product.service.ProductLikeService;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@WebMvcTest(controllers = UserProductLikeController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class UserProductLikeControllerTest {

    @MockBean
    private ProductLikeService productLikeService;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("상품 좋아요 추가")
    class 상품_좋아요_추가 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            given(productLikeService.addProductLike(productId)).willReturn(new UserProductLikeResDto(1));

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.post("/api/user/product/{productId}/like", productId)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(1));

            actions
                .andDo(document("product-like-add",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("productId").description("상품 ID")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("likeCount").description("좋아요 수")
                    )
                ));

            then(productLikeService).should().addProductLike(productId);
        }

        @Test
        @DisplayName("실패 - 이미 좋아요한 상품")
        void 실패_이미_좋아요한_상품() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.ALREADY_LIKED_PRODUCT)).given(productLikeService).addProductLike(productId);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.post("/api/user/product/{productId}/like", productId)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_LIKED_PRODUCT.getMessage()));

            actions
                .andDo(document("product-like-add-fail-already-liked",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("productId").description("상품 ID")
                    )
                ));

            then(productLikeService).should().addProductLike(productId);
        }

        @Test
        @DisplayName("실패 - 상품이 존재하지 않음")
        void 실패_상품이_존재하지_않음() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND)).given(productLikeService).addProductLike(productId);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.post("/api/user/product/{productId}/like", productId)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));

            actions
                .andDo(document("product-like-add-fail-product-not-found",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("productId").description("상품 ID")
                    )
                ));

            then(productLikeService).should().addProductLike(productId);
        }

        private final long productId = 1L;
    }

    @Nested
    @DisplayName("상품 좋아요 삭제")
    class 상품_좋아요_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            given(productLikeService.removeProductLike(productId)).willReturn(new UserProductLikeResDto(1));

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/user/product/{productId}/like", productId)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(1));

            actions
                .andDo(document("product-like-remove",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("productId").description("상품 ID")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("likeCount").description("좋아요 수")
                    )
                ));

            then(productLikeService).should().removeProductLike(productId);
        }

        @Test
        @DisplayName("실패 - 좋아요하지 않은 상품")
        void 실패_좋아요하지_않은_상품() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.NOT_LIKED_PRODUCT)).given(productLikeService).removeProductLike(productId);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/user/product/{productId}/like", productId)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_LIKED_PRODUCT.getMessage()));

            actions
                .andDo(document("product-like-remove-fail-not-liked",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("productId").description("상품 ID")
                    )
                ));

            then(productLikeService).should().removeProductLike(productId);
        }
    }

    private final long productId = 1L;
}