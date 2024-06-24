package potatowoong.potatomallback.domain.product.repository;

import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductSearchResDto;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

public interface ProductRepositoryCustom {

    PageResponseDto<ProductSearchResDto> findProductWithPage(PageRequestDto pageRequestDto);

    PageResponseDto<UserProductResDto.Search> findUserProductWithPage(PageRequestDto pageRequestDto);
}
