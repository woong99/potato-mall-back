package potatowoong.potatomallback.domain.pay.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@RequiredArgsConstructor
public class PayService {

    private final PayTransactionRepository payTransactionRepository;

    private final ProductRepository productRepository;

    private final TossPaymentPayTransactionRepository tossPaymentPayTransactionRepository;

    /**
     * 결제 가능 여부 확인
     */
    @Transactional
    public void checkAvailablePay(UserPayReqDto.CheckRequest dto) {
        // 상품 ID 파싱
        List<Long> productIds = dto.products().stream()
            .map(CheckProduct::productId)
            .toList();

        // 상품 정보 조회
        Map<Long, Product> savedProductMap = productRepository.findByProductIdIn(productIds).stream()
            .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        if (savedProductMap.size() != productIds.size()) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 결제 금액 계산
        final int totalAmount = getTotalAmount(dto, savedProductMap);

        // 결제 금액 검증
        if (totalAmount != dto.amount()) {
            throw new CustomException(ErrorCode.PAY_AMOUNT_NOT_MATCH);
        }

        // 결제 정보 - 결제 트랜잭션 정보 연결 테이블 저장
        TossPaymentPayTransaction tossPaymentPayTransaction = TossPaymentPayTransaction.builder()
            .orderId(dto.orderId())
            .build();
        tossPaymentPayTransactionRepository.save(tossPaymentPayTransaction);

        // 결제 트랜잭션 저장
        List<PayTransaction> payTransactions = dto.products().stream()
            .map(product -> PayTransaction.builder()
                .tossPaymentPayTransaction(tossPaymentPayTransaction)
                .product(savedProductMap.get(product.productId()))
                .quantity(product.quantity())
                .price(savedProductMap.get(product.productId()).getPrice())
                .build())
            .toList();
        payTransactionRepository.saveAll(payTransactions);
    }

    /**
     * 결제 후 파라미터 검증
     */
    @Transactional(readOnly = true)
    public void checkAfterPay(UserPayReqDto.VerifyPayment dto) {
        // 결제 트랜잭션 조회
        TossPaymentPayTransaction tossPaymentPayTransaction = tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionByOrderId(dto.orderId())
            .orElseThrow(() -> new CustomException(ErrorCode.PAY_TRANSACTION_NOT_FOUND));

        // 총 결제 금액 계산
        final int totalAmount = tossPaymentPayTransaction.getPayTransactions().stream()
            .mapToInt(transaction -> transaction.getPrice() * transaction.getQuantity())
            .sum();

        // 결제 금액 검증
        if (totalAmount != dto.amount()) {
            throw new CustomException(ErrorCode.PAY_AMOUNT_NOT_MATCH);
        }
    }

    /**
     * 총 결제 금액 계산
     */
    private int getTotalAmount(UserPayReqDto.CheckRequest dto, Map<Long, Product> savedProductMap) {
        return dto.products().stream()
            .mapToInt(product -> {
                Product savedProduct = savedProductMap.get(product.productId());

                // 재고량 검증
                if (product.quantity() > savedProduct.getStockQuantity()) {
                    throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
                }

                return savedProduct.getPrice() * product.quantity();
            })
            .sum();
    }

}
