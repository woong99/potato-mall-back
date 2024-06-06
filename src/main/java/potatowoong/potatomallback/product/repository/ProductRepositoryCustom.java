package potatowoong.potatomallback.product.repository;

import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.product.dto.response.ProductResDto.ProductSearchResDto;

public interface ProductRepositoryCustom {

    PageResponseDto<ProductSearchResDto> findProductWithPage(PageRequestDto pageRequestDto);
}