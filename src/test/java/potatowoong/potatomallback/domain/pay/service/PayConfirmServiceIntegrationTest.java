package potatowoong.potatomallback.domain.pay.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import potatowoong.potatomallback.domain.pay.entity.PayTransaction;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;
import potatowoong.potatomallback.domain.pay.repository.PayTransactionRepository;
import potatowoong.potatomallback.domain.pay.repository.TossPaymentPayTransactionRepository;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.entity.ProductCategory;
import potatowoong.potatomallback.domain.product.repository.ProductCategoryRepository;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@SpringBootTest
@ActiveProfiles("test")
@Disabled
@Slf4j
class PayConfirmServiceIntegrationTest {

    @Autowired
    private PayConfirmService payConfirmService;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TossPaymentPayTransactionRepository tossPaymentPayTransactionRepository;

    @Autowired
    private PayTransactionRepository payTransactionRepository;

    private void initData() {
        ProductCategory productCategory = initProductCategory();

        Product product1 = initProduct(productCategory);
        Product product2 = initProduct(productCategory);
        Product product3 = initProduct(productCategory);

        List<Product> products = Arrays.asList(product1, product2, product3);
        initTransaction("1", products);

        products = Arrays.asList(product2, product3, product1);
        initTransaction("2", products);

        products = Arrays.asList(product3, product2, product1);
        initTransaction("3", products);

        products = Arrays.asList(product3, product2, product1);
        initTransaction("4", products);

        products = Arrays.asList(product3, product2, product1);
        initTransaction("5", products);

        products = Arrays.asList(product3, product2, product1);
        initTransaction("6", products);

        products = Arrays.asList(product3, product2, product1);
        initTransaction("7", products);

        products = Arrays.asList(product3, product2, product1);
        initTransaction("8", products);

        products = Arrays.asList(product3, product2, product1);
        initTransaction("9", products);

        products = Arrays.asList(product3, product2, product1);
        initTransaction("10", products);
    }

    private ProductCategory initProductCategory() {
        ProductCategory productCategory = ProductCategory.builder()
            .name("테스트")
            .build();
        productCategoryRepository.save(productCategory);
        return productCategory;
    }

    private Product initProduct(ProductCategory productCategory) {
        Product product = Product.builder()
            .name("테스트")
            .content("테스트")
            .price(1000)
            .stockQuantity(20)
            .productCategory(productCategory)
            .build();
        productRepository.save(product);

        return product;
    }

    private void initTransaction(final String orderId, List<Product> products) {
        TossPaymentPayTransaction tossPaymentPayTransaction = TossPaymentPayTransaction.builder()
            .orderId(orderId)
            .build();
        tossPaymentPayTransactionRepository.save(tossPaymentPayTransaction);

        for (Product product : products) {
            PayTransaction payTransaction = PayTransaction.builder()
                .tossPaymentPayTransaction(tossPaymentPayTransaction)
                .product(product)
                .price(1000)
                .quantity(3)
                .build();
            payTransactionRepository.save(payTransaction);
        }
    }

    @BeforeEach
    void beforeEach() {
        payTransactionRepository.deleteAllInBatch();
        tossPaymentPayTransactionRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        productCategoryRepository.deleteAllInBatch();
        initData();
    }

    @AfterEach
    void afterEach() {
        payTransactionRepository.deleteAllInBatch();
        tossPaymentPayTransactionRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        productCategoryRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("상품 재고량 검증 및 감소")
    class 상품_재고량_검증_및_감소 {

        @DisplayName("동시성 테스트")
        @WithMockUser
        @RepeatedTest(10)
        void 동시성_테스트() throws InterruptedException {
            List<String> orderIds = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            List<Map<Long, Integer>> transactionMaps = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                transactionMaps.add(new HashMap<>());
            }

            ExecutorService executorService = Executors.newFixedThreadPool(32);
            CountDownLatch countDownLatch = new CountDownLatch(10);

            for (int i = 0; i < 10; i++) {
                Map<Long, Integer> transactionMap = transactionMaps.get(i);
                final String orderId = orderIds.get(i);

                executorService.submit(() -> {
                    try {
                        payConfirmService.modifyProductStockQuantity(transactionMap, orderId);
                    } catch (CustomException e) {
                        if (e.getErrorCode() != ErrorCode.PRODUCT_STOCK_NOT_ENOUGH) {
                            log.error("CustomException :: ", e);
                        }
                    } catch (Exception e) {
                        log.error("Exception :: ", e);
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            countDownLatch.await();

            List<Product> products = productRepository.findAll();

            Map<Long, Integer> resultMap = new HashMap<>();
            for (Map<Long, Integer> map : transactionMaps) {
                for (Map.Entry<Long, Integer> entry : map.entrySet()) {
                    resultMap.put(entry.getKey(), resultMap.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            }
            Product product1 = products.get(0);
            Product product2 = products.get(1);
            Product product3 = products.get(2);

            assertThat(product1.getStockQuantity()).isGreaterThanOrEqualTo(2);
            assertThat(product2.getStockQuantity()).isGreaterThanOrEqualTo(2);
            assertThat(product3.getStockQuantity()).isGreaterThanOrEqualTo(2);

            for (Map.Entry<Long, Integer> entry : resultMap.entrySet()) {
                assertThat(entry.getValue()).isLessThanOrEqualTo(18);
            }
        }
    }
}
