package potatowoong.potatomallback.domain.pay.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.cart.entity.ShoppingCart;
import potatowoong.potatomallback.domain.cart.repository.ShoppingCartRepository;
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

    private final ShoppingCartRepository shoppingCartRepository;

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

        // 장바구니 정보 조회
        Map<Long, ShoppingCart> shoppingCartMap = getShoppingCartMap(dto);

        // 결제 정보 - 결제 트랜잭션 정보 연결 테이블 저장
        TossPaymentPayTransaction tossPaymentPayTransaction = TossPaymentPayTransaction.builder()
            .orderId(dto.orderId())
            .build();
        tossPaymentPayTransactionRepository.save(tossPaymentPayTransaction);

        // 결제 트랜잭션 저장
        List<PayTransaction> payTransactions = new ArrayList<>();
        for (UserPayReqDto.CheckProduct checkProduct : dto.products()) {
            Product product = savedProductMap.get(checkProduct.productId());
            ShoppingCart shoppingCart = shoppingCartMap.get(checkProduct.shoppingCartId());

            PayTransaction payTransaction = PayTransaction.builder()
                .tossPaymentPayTransaction(tossPaymentPayTransaction)
                .product(product)
                .shoppingCart(shoppingCart)
                .quantity(checkProduct.quantity())
                .price(product.getPrice())
                .build();
            payTransactions.add(payTransaction);
        }
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

    /**
     * 장바구니 정보 조회
     */
    private Map<Long, ShoppingCart> getShoppingCartMap(UserPayReqDto.CheckRequest checkRequest) {
        // 장바구니 ID 파싱
        List<Long> shoppingCartIds = Optional.ofNullable(checkRequest.products())
            .orElse(Collections.emptyList())
            .stream()
            .map(CheckProduct::shoppingCartId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        // 장바구니 정보 조회
        List<ShoppingCart> shoppingCarts = shoppingCartIds.isEmpty() ? Collections.emptyList() : shoppingCartRepository.findByShoppingCartIdIn(shoppingCartIds);
        if (shoppingCartIds.size() != shoppingCarts.size()) {
            throw new CustomException(ErrorCode.SHOPPING_CART_NOT_FOUND);
        }

        return shoppingCarts.stream()
            .collect(Collectors.toMap(ShoppingCart::getShoppingCartId, Function.identity()));
    }
}
