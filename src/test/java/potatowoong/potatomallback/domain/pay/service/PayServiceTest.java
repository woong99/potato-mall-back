package potatowoong.potatomallback.domain.pay.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import potatowoong.potatomallback.domain.pay.dto.request.UserPayReqDto;
import potatowoong.potatomallback.domain.pay.dto.request.UserPayReqDto.CheckProduct;
import potatowoong.potatomallback.domain.pay.entity.PayTransaction;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;
import potatowoong.potatomallback.domain.pay.repository.PayTransactionRepository;
import potatowoong.potatomallback.domain.pay.repository.TossPaymentPayTransactionRepository;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class PayServiceTest {

    @Mock
    private PayTransactionRepository payTransactionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TossPaymentPayTransactionRepository tossPaymentPayTransactionRepository;

    @InjectMocks
    private PayService payService;

    @Nested
    @DisplayName("결제 가능 여부 확인")
    class 결제_가능_여부_확인 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            Product product1 = spy(Product.builder()
                .price(1000)
                .stockQuantity(1)
                .build());
            given(product1.getProductId()).willReturn(1L);

            Product product2 = spy(Product.builder()
                .price(2000)
                .stockQuantity(2)
                .build());
            given(product2.getProductId()).willReturn(2L);

            given(productRepository.findByProductIdIn(Arrays.asList(1L, 2L))).willReturn(Arrays.asList(product1, product2));

            // when
            payService.checkAvailablePay(checkRequest);

            // then
            then(productRepository).should().findByProductIdIn(any());
            then(tossPaymentPayTransactionRepository).should().save(any());
            then(payTransactionRepository).should().saveAll(any());
        }

        @Test
        @DisplayName("실패 - 상품 조회에 실패한 경우")
        void 실패_상품_조회에_실패한_경우() {
            // given
            given(productRepository.findByProductIdIn(Arrays.asList(1L, 2L))).willReturn(Collections.emptyList());

            // when
            assertThatThrownBy(() -> payService.checkAvailablePay(checkRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

            // then
            then(productRepository).should().findByProductIdIn(any());
            then(tossPaymentPayTransactionRepository).should(never()).save(any());
            then(payTransactionRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("실패 - 재고량이 부족한 경우")
        void 실패_재고량이_부족한_경우() {
            // given
            Product product1 = spy(Product.builder()
                .price(1000)
                .stockQuantity(1)
                .build());
            given(product1.getProductId()).willReturn(1L);

            Product product2 = spy(Product.builder()
                .price(2000)
                .stockQuantity(1)
                .build());
            given(product2.getProductId()).willReturn(2L);

            given(productRepository.findByProductIdIn(Arrays.asList(1L, 2L))).willReturn(Arrays.asList(product1, product2));

            // when
            assertThatThrownBy(() -> payService.checkAvailablePay(checkRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);

            // then
            then(productRepository).should().findByProductIdIn(any());
            then(tossPaymentPayTransactionRepository).should(never()).save(any());
            then(payTransactionRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("실패 - 결제 금액이 일치하지 않는 경우")
        void 실패_결제_금액이_일치하지_않는_경우() {
            // given
            Product product1 = spy(Product.builder()
                .price(1000)
                .stockQuantity(1)
                .build());
            given(product1.getProductId()).willReturn(1L);

            Product product2 = spy(Product.builder()
                .price(3000)
                .stockQuantity(2)
                .build());
            given(product2.getProductId()).willReturn(2L);

            given(productRepository.findByProductIdIn(Arrays.asList(1L, 2L))).willReturn(Arrays.asList(product1, product2));

            // when
            assertThatThrownBy(() -> payService.checkAvailablePay(checkRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAY_AMOUNT_NOT_MATCH);

            // then
            then(productRepository).should().findByProductIdIn(any());
            then(tossPaymentPayTransactionRepository).should(never()).save(any());
            then(payTransactionRepository).should(never()).saveAll(any());
        }

        private final List<UserPayReqDto.CheckProduct> checkProducts = Arrays.asList(
            CheckProduct.builder()
                .productId(1L)
                .quantity(1)
                .build(),
            CheckProduct.builder()
                .productId(2L)
                .quantity(2)
                .build()
        );

        private final UserPayReqDto.CheckRequest checkRequest = UserPayReqDto.CheckRequest.builder()
            .orderId("order-id")
            .products(checkProducts)
            .amount(5000)
            .build();
    }

    @Nested
    @DisplayName("결제 후 파라미터 검증")
    class 결제_후_파라미터_검증 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            TossPaymentPayTransaction tossPaymentPayTransaction = Mockito.spy(TossPaymentPayTransaction.builder()
                .orderId("order-id")
                .build());
            PayTransaction payTransaction1 = PayTransaction.builder()
                .price(10000)
                .quantity(1)
                .build();
            PayTransaction payTransaction2 = PayTransaction.builder()
                .price(20000)
                .quantity(2)
                .build();
            List<PayTransaction> payTransactions = Arrays.asList(payTransaction1, payTransaction2);
            given(tossPaymentPayTransaction.getPayTransactions()).willReturn(payTransactions);

            given(tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionByOrderId(dto.orderId())).willReturn(Optional.of(tossPaymentPayTransaction));

            // when
            payService.checkAfterPay(dto);

            // then
            then(tossPaymentPayTransactionRepository).should().findTossPaymentPayTransactionAndPayTransactionByOrderId(dto.orderId());
        }

        @Test
        @DisplayName("실패 - 결제 트랜잭션 정보가 존재하지 않는 경우")
        void 실패_결제_트랜잭션_정보가_존재하지_않는_경우() {
            // given
            given(tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionByOrderId(dto.orderId())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> payService.checkAfterPay(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAY_TRANSACTION_NOT_FOUND);

            // then
            then(tossPaymentPayTransactionRepository).should().findTossPaymentPayTransactionAndPayTransactionByOrderId(dto.orderId());
        }

        @Test
        @DisplayName("실패 - 결제 금액이 일치하지 않는 경우")
        void 실패_결제_금액이_일치하지_않는_경우() {
            // given
            TossPaymentPayTransaction tossPaymentPayTransaction = Mockito.spy(TossPaymentPayTransaction.builder()
                .orderId("order-id")
                .build());
            PayTransaction payTransaction1 = PayTransaction.builder()
                .price(10000)
                .quantity(1)
                .build();
            PayTransaction payTransaction2 = PayTransaction.builder()
                .price(30000)
                .quantity(2)
                .build();
            List<PayTransaction> payTransactions = Arrays.asList(payTransaction1, payTransaction2);
            given(tossPaymentPayTransaction.getPayTransactions()).willReturn(payTransactions);

            given(tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionByOrderId(dto.orderId())).willReturn(Optional.of(tossPaymentPayTransaction));

            // when
            assertThatThrownBy(() -> payService.checkAfterPay(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAY_AMOUNT_NOT_MATCH);

            // then
            then(tossPaymentPayTransactionRepository).should().findTossPaymentPayTransactionAndPayTransactionByOrderId(dto.orderId());

        }

        private final UserPayReqDto.VerifyPayment dto = UserPayReqDto.VerifyPayment.builder()
            .orderId("order-id")
            .amount(50000)
            .paymentKey("payment-key")
            .build();
    }
}