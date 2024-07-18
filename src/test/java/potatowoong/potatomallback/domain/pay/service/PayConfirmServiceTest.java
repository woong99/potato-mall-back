package potatowoong.potatomallback.domain.pay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.domain.cart.entity.ShoppingCart;
import potatowoong.potatomallback.domain.cart.repository.ShoppingCartRepository;
import potatowoong.potatomallback.domain.pay.dto.request.UserPayReqDto;
import potatowoong.potatomallback.domain.pay.entity.PayTransaction;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;
import potatowoong.potatomallback.domain.pay.repository.OrderHistoryRepository;
import potatowoong.potatomallback.domain.pay.repository.PayErrorLogRepository;
import potatowoong.potatomallback.domain.pay.repository.TossPaymentPayTransactionRepository;
import potatowoong.potatomallback.domain.pay.repository.TossPaymentRepository;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.service.UserProductService;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.global.exception.PayApiException;
import potatowoong.potatomallback.global.utils.TossPaymentUtils;

@ExtendWith(MockitoExtension.class)
class PayConfirmServiceTest {

    @Mock
    private TossPaymentPayTransactionRepository tossPaymentPayTransactionRepository;

    @Mock
    private TossPaymentRepository tossPaymentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @Mock
    private PayErrorLogRepository payErrorLogRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PayCancelService payCancelService;

    @Mock
    private UserProductService userProductService;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @InjectMocks
    private PayConfirmService payConfirmService;

    @Nested
    @DisplayName("상품 재고량 검증 및 감소")
    class 상품_재고량_검증_및_감소 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            Map<Long, Integer> transactionMap = new HashMap<>();

            PayTransaction payTransaction2 = PayTransaction.builder()
                .product(product2)
                .price(10000)
                .quantity(2)
                .build();

            given(product1.getProductId()).willReturn(1L);
            given(product2.getProductId()).willReturn(2L);
            given(tossPaymentPayTransaction.getPayTransactions()).willReturn(Arrays.asList(payTransaction1, payTransaction2));
            given(tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(orderId)).willReturn(Optional.of(tossPaymentPayTransaction));
            willDoNothing().given(userProductService).decreaseProductQuantityWithLock(1L, payTransaction1.getQuantity());
            willDoNothing().given(userProductService).decreaseProductQuantityWithLock(2L, payTransaction2.getQuantity());

            // when
            payConfirmService.modifyProductStockQuantity(transactionMap, orderId);

            // then
            then(tossPaymentPayTransactionRepository).should().findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(orderId);
            then(userProductService).should().decreaseProductQuantityWithLock(1L, 1);
            then(userProductService).should().decreaseProductQuantityWithLock(2L, 2);

            assertThat(transactionMap)
                .containsEntry(1L, 1)
                .containsEntry(2L, 2);
        }

        @Test
        @DisplayName("실패 - 결제 트랜잭션 정보가 없는 경우")
        void 실패_결제_트랜잭션_정보가_없는_경우() {
            // given
            Map<Long, Integer> transactionMap = new HashMap<>();

            given(tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(orderId)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> payConfirmService.modifyProductStockQuantity(transactionMap, orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAY_TRANSACTION_NOT_FOUND);

            // then
            then(tossPaymentPayTransactionRepository).should().findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(orderId);
            then(userProductService).should(never()).decreaseProductQuantityWithLock(anyLong(), anyInt());

            assertThat(transactionMap).isEmpty();
        }

        @Test
        @DisplayName("실패 - 재고량이 없는 경우")
        void 실패_재고량이_없는_경우() {
            // given
            Map<Long, Integer> transactionMap = new HashMap<>();

            PayTransaction payTransaction2 = PayTransaction.builder()
                .product(product2)
                .price(10000)
                .quantity(1)
                .build();

            given(product1.getProductId()).willReturn(1L);
            given(product2.getProductId()).willReturn(2L);
            given(tossPaymentPayTransaction.getPayTransactions()).willReturn(Arrays.asList(payTransaction1, payTransaction2));
            given(tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(orderId)).willReturn(Optional.of(tossPaymentPayTransaction));
            willDoNothing().given(userProductService).decreaseProductQuantityWithLock(1L, payTransaction1.getQuantity());
            willThrow(new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH)).given(userProductService).decreaseProductQuantityWithLock(2L, payTransaction2.getQuantity());

            // when
            assertThatThrownBy(() -> payConfirmService.modifyProductStockQuantity(transactionMap, orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);

            // then
            then(tossPaymentPayTransactionRepository).should().findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(orderId);
            then(userProductService).should().decreaseProductQuantityWithLock(1L, 1);
            then(userProductService).should().decreaseProductQuantityWithLock(2L, 1);

            assertThat(transactionMap).containsEntry(1L, 1);
        }

        @Test
        @DisplayName("실패 - 상품이 존재하지 않는 경우")
        void 실패_상품이_존재하지_않는_경우() {
            // given
            Map<Long, Integer> transactionMap = new HashMap<>();

            given(product1.getProductId()).willReturn(1L);
            given(tossPaymentPayTransaction.getPayTransactions()).willReturn(Collections.singletonList(payTransaction1));
            given(tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(orderId)).willReturn(Optional.of(tossPaymentPayTransaction));
            willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND)).given(userProductService).decreaseProductQuantityWithLock(1L, payTransaction1.getQuantity());

            // when
            assertThatThrownBy(() -> payConfirmService.modifyProductStockQuantity(transactionMap, orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

            // then
            then(tossPaymentPayTransactionRepository).should().findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(orderId);
            then(userProductService).should().decreaseProductQuantityWithLock(1L, payTransaction1.getQuantity());

            assertThat(transactionMap).isEmpty();
        }

        private final String orderId = "orderId";

        private final Product product1 = spy(Product.builder()
            .stockQuantity(1)
            .price(10000)
            .build());

        private final Product product2 = spy(Product.builder()
            .stockQuantity(2)
            .price(20000)
            .build());

        private final PayTransaction payTransaction1 = PayTransaction.builder()
            .product(product1)
            .price(10000)
            .quantity(1)
            .build();

        private final TossPaymentPayTransaction tossPaymentPayTransaction = spy(TossPaymentPayTransaction.builder()
            .orderId(orderId)
            .build());
    }

    @Nested
    @DisplayName("결제 승인 API 호출")
    @SuppressWarnings("unchecked")
    class 결제_승인_API_호출 {

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(payConfirmService, tossSecretKey, tossSecretKey);
        }

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(tossPaymentPayTransaction.getPayTransactions()).willReturn(Collections.singletonList(payTransaction));
            given(restTemplate.exchange(apiUrl, HttpMethod.POST, getRequest(), JSONObject.class)).willReturn(ResponseEntity.ok(getSuccessJsonObject()));

            // when
            payConfirmService.payConfirm(tossPaymentPayTransaction, verifyPayment);

            // then
            then(restTemplate).should().exchange(apiUrl, HttpMethod.POST, getRequest(), JSONObject.class);
            then(memberRepository).should().getReferenceById(any());
            then(tossPaymentRepository).should().save(any());
            then(orderHistoryRepository).should().save(any());
            then(payErrorLogRepository).should(never()).save(any());
            then(payCancelService).should(never()).fetchNetCancelApi(any(), any());
        }

        @Test
        @DisplayName("실패 - 결제 승인이 실패한 경우")
        void 실패_결제_승인이_실패한_경우() {
            // given
            given(restTemplate.exchange(apiUrl, HttpMethod.POST, getRequest(), JSONObject.class)).willReturn(ResponseEntity.badRequest().body(getFailJsonObject()));

            // when
            assertThatThrownBy(() -> payConfirmService.payConfirm(tossPaymentPayTransaction, verifyPayment))
                .isInstanceOf(PayApiException.class);

            // then
            then(restTemplate).should().exchange(apiUrl, HttpMethod.POST, getRequest(), JSONObject.class);
            then(memberRepository).should(never()).getReferenceById(any());
            then(tossPaymentRepository).should(never()).save(any());
            then(orderHistoryRepository).should(never()).save(any());
            then(payErrorLogRepository).should().save(any());
            then(payCancelService).should().fetchNetCancelApi(any(), any());
        }

        @Test
        @DisplayName("실패 - 결제 승인 전 기타 오류가 발생한 경우")
        void 실패_결제_승인_전_기타_오류가_발생한_경우() {
            // given
            given(restTemplate.exchange(apiUrl, HttpMethod.POST, getRequest(), JSONObject.class)).willThrow(new RuntimeException());

            // when
            assertThatThrownBy(() -> payConfirmService.payConfirm(tossPaymentPayTransaction, verifyPayment))
                .isInstanceOf(PayApiException.class);

            // then
            then(restTemplate).should().exchange(apiUrl, HttpMethod.POST, getRequest(), JSONObject.class);
            then(memberRepository).should(never()).getReferenceById(any());
            then(tossPaymentRepository).should(never()).save(any());
            then(orderHistoryRepository).should(never()).save(any());
            then(payErrorLogRepository).should(never()).save(any());
            then(payCancelService).should(never()).fetchNetCancelApi(any(), any());
        }

        @Test
        @DisplayName("실패 - 결제 승인 후 기타 오류가 발생한 경우")
        void 실패_결제_승인_후_기타_오류가_발생한_경우() {
            // given
            given(restTemplate.exchange(apiUrl, HttpMethod.POST, getRequest(), JSONObject.class)).willReturn(ResponseEntity.ok(getSuccessJsonObject()));
            given(tossPaymentRepository.save(any())).willThrow(new RuntimeException());

            // when
            assertThatThrownBy(() -> payConfirmService.payConfirm(tossPaymentPayTransaction, verifyPayment))
                .isInstanceOf(PayApiException.class);

            // then
            then(restTemplate).should().exchange(apiUrl, HttpMethod.POST, getRequest(), JSONObject.class);
            then(memberRepository).should().getReferenceById(any());
            then(tossPaymentRepository).should().save(any());
            then(orderHistoryRepository).should(never()).save(any());
            then(payErrorLogRepository).should(never()).save(any());
            then(payCancelService).should().fetchNetCancelApi(any(), any());
        }

        private final UserPayReqDto.VerifyPayment verifyPayment = UserPayReqDto.VerifyPayment.builder()
            .paymentKey("paymentKey")
            .orderId("orderId")
            .amount(10000)
            .build();

        private final TossPaymentPayTransaction tossPaymentPayTransaction = spy(TossPaymentPayTransaction.builder()
            .orderId("orderId")
            .build());

        private final PayTransaction payTransaction = PayTransaction.builder()
            .product(Product.builder().build())
            .quantity(1)
            .price(1000)
            .build();

        private final String apiUrl = "https://api.tosspayments.com/v1/payments/confirm";

        private final String tossSecretKey = "tossSecretKey";

        private HttpEntity<String> getRequest() {
            JSONObject obj = new JSONObject();
            obj.put("orderId", verifyPayment.orderId());
            obj.put("amount", verifyPayment.amount());
            obj.put("paymentKey", verifyPayment.paymentKey());

            HttpHeaders headers = TossPaymentUtils.getCommonApiHeaders(tossSecretKey);
            return new HttpEntity<>(obj.toString(), headers);
        }

        private JSONObject getSuccessJsonObject() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("paymentKey", "paymentKey");
            jsonObject.put("requestedAt", "2024-07-15T22:22:39+01:00");
            jsonObject.put("approvedAt", "2024-07-15T22:22:39+01:00");

            return jsonObject;
        }

        private JSONObject getFailJsonObject() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("paymentKey", "paymentKey");
            jsonObject.put("code", "code");
            jsonObject.put("message", "message");

            return jsonObject;
        }
    }

    @Nested
    @DisplayName("장바구니 정보 삭제")
    class 장바구니_정보_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            final String orderId = "orderId";

            given(shoppingCart.getShoppingCartId()).willReturn(1L);
            given(tossPaymentPayTransaction.getPayTransactions()).willReturn(Collections.singletonList(payTransaction));
            given(tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionAndProductAndShoppingCartByOrderId(orderId)).willReturn(Optional.of(tossPaymentPayTransaction));

            // when
            payConfirmService.removeShoppingCart(orderId);

            // then
            then(tossPaymentPayTransactionRepository).should().findTossPaymentPayTransactionAndPayTransactionAndProductAndShoppingCartByOrderId(orderId);
            then(shoppingCartRepository).should().deleteByShoppingCartIdIn(Collections.singletonList(1L));
        }

        private final ShoppingCart shoppingCart = spy(ShoppingCart.builder()
            .build());

        private final PayTransaction payTransaction = PayTransaction.builder()
            .shoppingCart(shoppingCart)
            .build();

        private final TossPaymentPayTransaction tossPaymentPayTransaction = spy(TossPaymentPayTransaction.builder()
            .orderId("orderId")
            .build());

    }
}