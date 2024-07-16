package potatowoong.potatomallback.domain.product.repository;

import java.util.Optional;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductSearchResDto;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

public interface ProductRepositoryCustom {

    PageResponseDto<ProductSearchResDto> findProductWithPage(PageRequestDto pageRequestDto);

    PageResponseDto<UserProductResDto.Search> findUserProductWithPage(PageRequestDto pageRequestDto);

    Optional<UserProductResDto.Detail> findUserProductById(long productId);

    Optional<Product> findByIdWithLock(long productId);
}
