package potatowoong.potatomallback.domain.product.repository;

import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategorySearchResDto;

public interface ProductCategoryRepositoryCustom {

    PageResponseDto<ProductCategorySearchResDto> findProductCategoryWithPage(PageRequestDto pageRequestDto);
}
