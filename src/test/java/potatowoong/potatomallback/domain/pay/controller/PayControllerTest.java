package potatowoong.potatomallback.domain.pay.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentRequest;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
import potatowoong.potatomallback.domain.pay.dto.request.UserPayReqDto;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;
import potatowoong.potatomallback.domain.pay.service.PayConfirmService;
import potatowoong.potatomallback.domain.pay.service.PayService;
import potatowoong.potatomallback.domain.product.service.UserProductService;
import potatowoong.potatomallback.global.common.ResponseText;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.global.exception.PayApiException;

@WebMvcTest(controllers = PayController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class PayControllerTest {

    @MockBean
    private PayService payService;

    @MockBean
    private PayConfirmService payConfirmService;

    @MockBean
    private UserProductService userProductService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("결제 가능 여부 확인 API")
    class 결제_가능_여부_확인_API {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // when
            ResultActions actions = mockMvc.perform(post("/api/user/pay/check-available")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest)));

            // then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_CHECK_AVAILABLE_PAY));

            actions
                .andDo(document("pay-check-available",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("orderId").optional().description("주문 ID"),
                        fieldWithPath("products").optional().description("상품 정보"),
                        fieldWithPath("products[].productId").optional().description("상품 ID"),
                        fieldWithPath("products[].quantity").optional().description("상품 수량"),
                        fieldWithPath("amount").optional().description("결제 금액")
                    )
                ));

            then(payService).should().checkAvailablePay(checkRequest);
        }

        @Test
        @DisplayName("실패 - 상품 정보가 존재하지 않는 경우")
        void 실패_상품_정보가_존재하지_않는_경우() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND)).given(payService).checkAvailablePay(checkRequest);

            // when
            ResultActions actions = mockMvc.perform(post("/api/user/pay/check-available")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest)));

            // then
            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));

            actions
                .andDo(document("pay-check-available-not-found-product",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("orderId").optional().description("주문 ID"),
                        fieldWithPath("products").optional().description("상품 정보"),
                        fieldWithPath("products[].productId").optional().description("상품 ID"),
                        fieldWithPath("products[].quantity").optional().description("상품 수량"),
                        fieldWithPath("amount").optional().description("결제 금액")
                    )
                ));

            then(payService).should().checkAvailablePay(checkRequest);
        }

        @Test
        @DisplayName("실패 - 결제 금액이 일치하지 않는 경우")
        void 실패_결제_금액이_일치하지_않는_경우() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PAY_AMOUNT_NOT_MATCH)).given(payService).checkAvailablePay(checkRequest);

            // when
            ResultActions actions = mockMvc.perform(post("/api/user/pay/check-available")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest)));

            // then
            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PAY_AMOUNT_NOT_MATCH.getMessage()));

            actions
                .andDo(document("pay-check-available-amount-not-match",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("orderId").optional().description("주문 ID"),
                        fieldWithPath("products").optional().description("상품 정보"),
                        fieldWithPath("products[].productId").optional().description("상품 ID"),
                        fieldWithPath("products[].quantity").optional().description("상품 수량"),
                        fieldWithPath("amount").optional().description("결제 금액")
                    )
                ));

            then(payService).should().checkAvailablePay(checkRequest);
        }

        private final UserPayReqDto.CheckRequest checkRequest = UserPayReqDto.CheckRequest.builder()
            .orderId("orderId")
            .products(Collections.singletonList(UserPayReqDto.CheckProduct.builder()
                .productId(1L)
                .quantity(1)
                .build()))
            .amount(1000)
            .build();
    }

    @Nested
    @DisplayName("결제 후 파라미터 검증 API")
    class 결제_후_파라미터_검증_API {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // when
            ResultActions actions = mockMvc.perform(post("/api/user/pay/verify-payment")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyPayment)));

            // then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_CHECK_AFTER_PAY));

            actions
                .andDo(document("pay-verify-payment",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("paymentKey").optional().description("결제 키"),
                        fieldWithPath("orderId").optional().description("주문 ID"),
                        fieldWithPath("amount").optional().description("결제 금액")
                    )
                ));

            then(payService).should().checkAfterPay(verifyPayment);
        }

        @Test
        @DisplayName("실패 - 결제 트랜잭션 정보가 없는 경우")
        void 실패_결제_트랜잭션_정보가_없는_경우() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PAY_TRANSACTION_NOT_FOUND)).given(payService).checkAfterPay(verifyPayment);

            // when
            ResultActions actions = mockMvc.perform(post("/api/user/pay/verify-payment")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyPayment)));

            // then
            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PAY_TRANSACTION_NOT_FOUND.getMessage()));

            actions
                .andDo(document("pay-verify-payment-not-found-transaction",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("paymentKey").optional().description("결제 키"),
                        fieldWithPath("orderId").optional().description("주문 ID"),
                        fieldWithPath("amount").optional().description("결제 금액")
                    )
                ));

            then(payService).should().checkAfterPay(verifyPayment);
        }

        @Test
        @DisplayName("실패 - 결제 금액이 일치하지 않는 경우")
        void 실패_결제_금액이_일치하지_않는_경우() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PAY_AMOUNT_NOT_MATCH)).given(payService).checkAfterPay(verifyPayment);

            // when
            ResultActions actions = mockMvc.perform(post("/api/user/pay/verify-payment")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyPayment)));

            // then
            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PAY_AMOUNT_NOT_MATCH.getMessage()));

            actions
                .andDo(document("pay-verify-payment-amount-not-match",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("paymentKey").optional().description("결제 키"),
                        fieldWithPath("orderId").optional().description("주문 ID"),
                        fieldWithPath("amount").optional().description("결제 금액")
                    )
                ));

            then(payService).should().checkAfterPay(verifyPayment);
        }

        private final UserPayReqDto.VerifyPayment verifyPayment = UserPayReqDto.VerifyPayment.builder()
            .paymentKey("paymentKey")
            .orderId("orderId")
            .amount(1000)
            .build();
    }

    @Nested
    @DisplayName("결제 승인 API")
    class 결제_승인_API {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            given(payConfirmService.modifyProductStockQuantity(transactionMap, verifyPayment.orderId())).willReturn(tossPaymentPayTransaction);
            willDoNothing().given(payConfirmService).payConfirm(tossPaymentPayTransaction, verifyPayment);

            // when
            ResultActions actions = mockMvc.perform(post("/api/user/pay/confirm")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyPayment)));

            // then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_PAY));

            actions
                .andDo(document("pay-confirm",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("paymentKey").optional().description("결제 키"),
                        fieldWithPath("orderId").optional().description("주문 ID"),
                        fieldWithPath("amount").optional().description("결제 금액")
                    )
                ));

            then(payConfirmService).should().modifyProductStockQuantity(transactionMap, verifyPayment.orderId());
            then(payConfirmService).should().payConfirm(tossPaymentPayTransaction, verifyPayment);
        }

        @Test
        @DisplayName("실패 - 재고량 확인 및 수정 과정에서 오류 발생")
        void 실패_재고량_확인_및_수정_과정에서_오류_발생() throws Exception {
            // given
            willThrow(new PayApiException()).given(payConfirmService).modifyProductStockQuantity(transactionMap, verifyPayment.orderId());

            // when
            ResultActions actions = mockMvc.perform(post("/api/user/pay/confirm")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyPayment)));

            // then
            actions
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(ErrorCode.PAYMENT_FAILED.getMessage()));

            actions
                .andDo(document("pay-confirm-failed-modify-stock-quantity",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("paymentKey").optional().description("결제 키"),
                        fieldWithPath("orderId").optional().description("주문 ID"),
                        fieldWithPath("amount").optional().description("결제 금액")
                    )
                ));

            then(payConfirmService).should().modifyProductStockQuantity(transactionMap, verifyPayment.orderId());
            then(payConfirmService).should(never()).payConfirm(tossPaymentPayTransaction, verifyPayment);
        }

        @Test
        @DisplayName("실패 - 결제 승인 API 호출 과정에서 오류 발생")
        void 실패_결제_승인_API_호출_과정에서_오류_발생() throws Exception {
            // given
            given(payConfirmService.modifyProductStockQuantity(transactionMap, verifyPayment.orderId())).willReturn(tossPaymentPayTransaction);
            willThrow(new PayApiException()).given(payConfirmService).payConfirm(tossPaymentPayTransaction, verifyPayment);

            // when
            ResultActions actions = mockMvc.perform(post("/api/user/pay/confirm")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyPayment)));

            // then
            actions
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(ErrorCode.PAYMENT_FAILED.getMessage()));

            actions
                .andDo(document("pay-confirm-failed-pay-confirm",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("paymentKey").optional().description("결제 키"),
                        fieldWithPath("orderId").optional().description("주문 ID"),
                        fieldWithPath("amount").optional().description("결제 금액")
                    )
                ));

            then(payConfirmService).should().modifyProductStockQuantity(transactionMap, verifyPayment.orderId());
            then(payConfirmService).should().payConfirm(tossPaymentPayTransaction, verifyPayment);
        }

        private final Map<Long, Integer> transactionMap = new HashMap<>();

        private final UserPayReqDto.VerifyPayment verifyPayment = UserPayReqDto.VerifyPayment.builder()
            .paymentKey("paymentKey")
            .orderId("orderId")
            .amount(1000)
            .build();

        private final TossPaymentPayTransaction tossPaymentPayTransaction = TossPaymentPayTransaction.builder()
            .orderId("orderId")
            .build();
    }
}