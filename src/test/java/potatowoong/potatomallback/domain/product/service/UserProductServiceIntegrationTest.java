package potatowoong.potatomallback.domain.product.service;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.entity.ProductCategory;
import potatowoong.potatomallback.domain.product.repository.ProductCategoryRepository;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@SpringBootTest
@ActiveProfiles("test")
@Disabled
@WithMockUser
@Slf4j
class UserProductServiceIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private UserProductService userProductService;

    @BeforeEach
    void setUp() {
        ProductCategory productCategory = ProductCategory.builder()
            .name("category")
            .build();
        productCategoryRepository.save(productCategory);

        Product product = Product.builder()
            .stockQuantity(25)
            .price(1000)
            .name("product")
            .content("content")
            .productCategory(productCategory)
            .build();
        productRepository.save(product);
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        productCategoryRepository.deleteAll();
    }

    @Test
    @DisplayName("상품 재고량 감소 + 베타락")
    void 상품_재고량_감소_베타락() throws InterruptedException {
        // given
        Product product = productRepository.findAll().get(0);

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(20);

        for (int i = 0; i < 20; i++) {
            final long productId = product.getProductId();
            executorService.submit(() -> {
                try {
                    userProductService.decreaseProductQuantityWithLock(productId, 1);
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

        // then
        product = productRepository.findAll().get(0);
        assertThat(product.getStockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("상품 재고량 복원 + 베타락")
    void 상품_재고량_복원_베타락() throws InterruptedException {
        // given
        Product product = productRepository.findAll().get(0);

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(20);

        for (int i = 0; i < 20; i++) {
            final long productId = product.getProductId();
            executorService.submit(() -> {
                try {
                    userProductService.increaseProductQuantityWithLock(productId, 1);
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

        // then
        product = productRepository.findAll().get(0);
        assertThat(product.getStockQuantity()).isEqualTo(45);
    }
}
