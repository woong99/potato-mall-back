package potatowoong.potatomallback.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto.Search;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.config.redis.DistributeLock;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class UserProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<Search> getUserProductList(PageRequestDto pageRequestDto) {
        return productRepository.findUserProductWithPage(pageRequestDto);
    }

    /**
     * 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public UserProductResDto.Detail getUserProduct(final long productId) {
        return productRepository.findUserProductById(productId)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 상품 재고량 감소 + 분산락
     */
    @DistributeLock(key = "T(java.lang.String).format('Product%d', #productId)")
    public void decreaseProductQuantityWithLock(final long productId, final int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 재고량 검증
        if (quantity > product.getStockQuantity()) {
            throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
        }

        product.decreaseStockQuantity(quantity);
        productRepository.save(product);
    }

    /**
     * 상품 재고량 복원 + 분산락
     */
    @DistributeLock(key = "T(java.lang.String).format('Product%d', #productId)")
    public void increaseProductQuantityWithLock(final long productId, final int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        product.increaseStockQuantity(quantity);
        productRepository.save(product);
    }
}
