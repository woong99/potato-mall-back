package potatowoong.potatomallback.domain.cart.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentRequest;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.cart.dto.request.UserShoppingCartReqDto;
import potatowoong.potatomallback.domain.cart.dto.response.UserShoppingCartResDto;
import potatowoong.potatomallback.domain.cart.service.UserShoppingCartService;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto;
import potatowoong.potatomallback.global.common.ResponseText;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@WebMvcTest(UserShoppingCartController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class UserShoppingCartControllerTest {

    @MockBean
    private UserShoppingCartService userShoppingCartService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("자신의 장바구니 상품 개수 조회")
    class 자신의_장바구니_상품_개수_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            given(userShoppingCartService.getShoppingCartCount()).willReturn(1);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/shopping-cart/me/items-count")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));

            actions
                .andDo(document("user-shopping-cart-count",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            // then
            then(userShoppingCartService).should().getShoppingCartCount();
        }
    }

    @Nested
    @DisplayName("자신의 장바구니 상품 목록 조회")
    class 자신의_장바구니_상품_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            given(userShoppingCartService.getShoppingCartList()).willReturn(Collections.singletonList(dto));

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/shopping-cart/me/items")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

            actions
                .andDo(document("user-shopping-cart-list",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("product.productId").description("상품 ID"),
                        fieldWithPath("product.name").description("상품명"),
                        fieldWithPath("product.price").description("가격"),
                        fieldWithPath("product.thumbnailUrl").description("썸네일 URL"),
                        fieldWithPath("product.stockQuantity").description("재고량"),
                        fieldWithPath("shoppingCartId").description("장바구니 ID"),
                        fieldWithPath("quantity").description("수량")
                    )
                ));

            // then
            then(userShoppingCartService).should().getShoppingCartList();
        }

        private final UserProductResDto.CartProduct product = UserProductResDto.CartProduct.builder()
            .productId(1L)
            .name("product")
            .price(1000)
            .thumbnailUrl("image")
            .stockQuantity(1)
            .build();

        private final UserShoppingCartResDto.DetailWithProduct dto = UserShoppingCartResDto.DetailWithProduct.builder()
            .product(product)
            .shoppingCartId(1L)
            .quantity(1)
            .build();
    }

    @Nested
    @DisplayName("장바구니 상품 상세 조회")
    class 장바구니_상품_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            given(userShoppingCartService.getShoppingCart(1L)).willReturn(dto);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/shopping-cart/{shoppingCartId}", 1L)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shoppingCartId").value(1L))
                .andExpect(jsonPath("$.data.quantity").value(1));

            actions
                .andDo(document("user-shopping-cart-detail",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("shoppingCartId").description("장바구니 ID"),
                        fieldWithPath("quantity").description("수량")
                    )
                ));

            // then
            then(userShoppingCartService).should().getShoppingCart(1L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 장바구니 상품")
        void 실패_존재하지_않는_장바구니_상품() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.SHOPPING_CART_NOT_FOUND)).given(userShoppingCartService).getShoppingCart(1L);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/shopping-cart/{shoppingCartId}", 1L)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.SHOPPING_CART_NOT_FOUND.getMessage()));

            actions
                .andDo(document("user-shopping-cart-detail-fail-shopping-cart-not-found",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            // then
            then(userShoppingCartService).should().getShoppingCart(1L);
        }

        private final UserShoppingCartResDto.Detail dto = UserShoppingCartResDto.Detail.builder()
            .shoppingCartId(1L)
            .quantity(1)
            .build();
    }

    @Nested
    @DisplayName("장바구니 상품 등록")
    class 장바구니_상품_등록 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            willDoNothing().given(userShoppingCartService).addShoppingCart(dto);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/shopping-cart")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_ADD_SHOPPING_CART));

            actions
                .andDo(document("user-shopping-cart-add",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("quantity").optional().description("수량")
                    )
                ));

            then(userShoppingCartService).should().addShoppingCart(dto);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품")
        void 실패_존재하지_않는_상품() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND)).given(userShoppingCartService).addShoppingCart(dto);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/shopping-cart")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));

            actions
                .andDo(document("user-shopping-cart-add-fail-product-not-found",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("quantity").optional().description("수량")
                    )
                ));

            then(userShoppingCartService).should().addShoppingCart(dto);
        }

        @Test
        @DisplayName("실패 - 상품 재고량 초과")
        void 실패_매진된_상품() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PRODUCT_QUANTITY_EXCEEDED)).given(userShoppingCartService).addShoppingCart(dto);

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/shopping-cart")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_QUANTITY_EXCEEDED.getMessage()));

            actions
                .andDo(document("user-shopping-cart-add-fail-product-sold-out",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("quantity").optional().description("수량")
                    )
                ));

            then(userShoppingCartService).should().addShoppingCart(dto);
        }

        private final UserShoppingCartReqDto.Add dto = UserShoppingCartReqDto.Add.builder()
            .productId(1L)
            .quantity(1)
            .build();
    }

    @Nested
    @DisplayName("장바구니 상품 수정")
    class 장바구니_상품_수정 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            willDoNothing().given(userShoppingCartService).modifyShoppingCart(dto);

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/user/shopping-cart")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_MODIFY_SHOPPING_CART));

            actions
                .andDo(document("user-shopping-cart-modify",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("shoppingCartId").optional().description("장바구니 ID"),
                        fieldWithPath("quantity").optional().description("수량")
                    )
                ));

            then(userShoppingCartService).should().modifyShoppingCart(dto);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 장바구니 상품")
        void 실패_존재하지_않는_장바구니_상품() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.SHOPPING_CART_NOT_FOUND)).given(userShoppingCartService).modifyShoppingCart(dto);

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/user/shopping-cart")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.SHOPPING_CART_NOT_FOUND.getMessage()));

            actions
                .andDo(document("user-shopping-cart-modify-fail-shopping-cart-not-found",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("shoppingCartId").optional().description("장바구니 ID"),
                        fieldWithPath("quantity").optional().description("수량")
                    )
                ));

            then(userShoppingCartService).should().modifyShoppingCart(dto);
        }

        @Test
        @DisplayName("실패 - 상품 재고량 초과")
        void 실패_매진된_상품() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PRODUCT_QUANTITY_EXCEEDED)).given(userShoppingCartService).modifyShoppingCart(dto);

            // when & then
            ResultActions actions = mockMvc.perform(put("/api/user/shopping-cart")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_QUANTITY_EXCEEDED.getMessage()));

            actions
                .andDo(document("user-shopping-cart-modify-fail-product-sold-out",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("shoppingCartId").optional().description("장바구니 ID"),
                        fieldWithPath("quantity").optional().description("수량")
                    )
                ));

            then(userShoppingCartService).should().modifyShoppingCart(dto);
        }

        private final UserShoppingCartReqDto.Modify dto = UserShoppingCartReqDto.Modify.builder()
            .shoppingCartId(1L)
            .quantity(1)
            .build();
    }

    @Nested
    @DisplayName("장바구니 상품 삭제")
    class 장바구니_상품_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            willDoNothing().given(userShoppingCartService).removeShoppingCart(1L);

            // when & then
            ResultActions actions = mockMvc.perform(delete("/api/user/shopping-cart/{shoppingCartId}", 1L)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_REMOVE_SHOPPING_CART));

            actions
                .andDo(document("user-shopping-cart-remove",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            then(userShoppingCartService).should().removeShoppingCart(1L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 장바구니 상품")
        void 실패_존재하지_않는_장바구니_상품() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.SHOPPING_CART_NOT_FOUND)).given(userShoppingCartService).removeShoppingCart(1L);

            // when & then
            ResultActions actions = mockMvc.perform(delete("/api/user/shopping-cart/{shoppingCartId}", 1L)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.SHOPPING_CART_NOT_FOUND.getMessage()));

            actions
                .andDo(document("user-shopping-cart-remove-fail-shopping-cart-not-found",
                    getDocumentRequest(),
                    getDocumentResponse()
                ));

            then(userShoppingCartService).should().removeShoppingCart(1L);
        }
    }
}