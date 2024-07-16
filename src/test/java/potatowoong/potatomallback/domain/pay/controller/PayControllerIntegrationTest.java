package potatowoong.potatomallback.domain.pay.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.pay.dto.request.UserPayReqDto.VerifyPayment;
import potatowoong.potatomallback.domain.pay.entity.PayTransaction;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;
import potatowoong.potatomallback.domain.pay.repository.PayTransactionRepository;
import potatowoong.potatomallback.domain.pay.repository.TossPaymentPayTransactionRepository;
import potatowoong.potatomallback.domain.pay.service.PayConfirmService;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.entity.ProductCategory;
import potatowoong.potatomallback.domain.product.repository.ProductCategoryRepository;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.domain.product.service.UserProductService;
import potatowoong.potatomallback.global.exception.PayApiException;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Slf4j
@ActiveProfiles("test")
@Disabled
class PayControllerIntegrationTest {

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TossPaymentPayTransactionRepository tossPaymentPayTransactionRepository;

    @Autowired
    private PayTransactionRepository payTransactionRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private PayConfirmService payConfirmService;

    @SpyBean
    private UserProductService userProductService;

    private void initData() {
        Product product1 = initProduct();
        Product product2 = initProduct();
        Product product3 = initProduct();

        List<Product> products = Arrays.asList(product1, product2, product3);
        initTransaction(products);
    }

    private ProductCategory initProductCategory() {
        ProductCategory productCategory = ProductCategory.builder()
            .name("category")
            .build();

        return productCategoryRepository.save(productCategory);
    }

    private Product initProduct() {
        ProductCategory productCategory = initProductCategory();

        Product product = Product.builder()
            .name("product")
            .content("content")
            .price(1000)
            .stockQuantity(10)
            .productCategory(productCategory)
            .build();

        return productRepository.save(product);
    }

    private void initTransaction(List<Product> products) {
        TossPaymentPayTransaction tossPaymentPayTransaction = TossPaymentPayTransaction.builder()
            .orderId("1")
            .build();
        tossPaymentPayTransactionRepository.save(tossPaymentPayTransaction);

        for (Product product : products) {
            PayTransaction payTransaction = PayTransaction.builder()
                .tossPaymentPayTransaction(tossPaymentPayTransaction)
                .product(product)
                .price(1000)
                .quantity(3)
                .build();
            payTransactionRepository.saveAndFlush(payTransaction);
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

    @Test
    @DisplayName("성공")
    void 성공() throws Exception {
        // given
        willDoNothing().given(payConfirmService).payConfirm(any(), any());

        // when
        ResultActions actions = mockMvc.perform(post("/api/user/pay/confirm")
            .content(objectMapper.writeValueAsString(verifyPayment))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isOk());

        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            assertThat(product.getStockQuantity()).isEqualTo(7);
        }

        then(payConfirmService).should().modifyProductStockQuantity(any(), any());
        then(payConfirmService).should().payConfirm(any(), any());
        then(userProductService).should(never()).increaseProductQuantityWithLock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("실패 - 결제 승인 API 호출 중 오류")
    void 실패_결제_승인_API_호출_중_오류() throws Exception {
        // given
        willThrow(new PayApiException()).given(payConfirmService).payConfirm(any(), any());

        // when
        ResultActions actions = mockMvc.perform(post("/api/user/pay/confirm")
            .content(objectMapper.writeValueAsString(verifyPayment))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isInternalServerError());

        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            assertThat(product.getStockQuantity()).isEqualTo(10);
        }

        then(payConfirmService).should().modifyProductStockQuantity(any(), any());
        then(payConfirmService).should().payConfirm(any(), any());
        then(userProductService).should(times(3)).increaseProductQuantityWithLock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("실패 - 재고량 확인 및 수정 중 재고량이 부족한 경우")
    void 실패_재고량_확인_및_수정_중_재고량이_부족한_경우() throws Exception {
        // given
        willThrow(new PayApiException()).given(payConfirmService).payConfirm(any(), any());

        // when
        ResultActions actions = mockMvc.perform(post("/api/user/pay/confirm")
            .content(objectMapper.writeValueAsString(verifyPayment))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isInternalServerError());

        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            assertThat(product.getStockQuantity()).isEqualTo(10);
        }

        then(payConfirmService).should().modifyProductStockQuantity(any(), any());
        then(payConfirmService).should().payConfirm(any(), any());
        then(userProductService).should(times(3)).increaseProductQuantityWithLock(anyLong(), anyInt());
    }

    private final VerifyPayment verifyPayment = VerifyPayment.builder()
        .orderId("1")
        .amount(3000)
        .paymentKey("key")
        .build();
}
