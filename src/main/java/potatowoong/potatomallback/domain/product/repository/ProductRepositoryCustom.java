package potatowoong.potatomallback.domain.product.repository;

import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductSearchResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.UserProductSearchResDto;

public interface ProductRepositoryCustom {

    PageResponseDto<ProductSearchResDto> findProductWithPage(PageRequestDto pageRequestDto);

    PageResponseDto<UserProductSearchResDto> findUserProductWithPage(PageRequestDto pageRequestDto);
}
