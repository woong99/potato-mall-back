package potatowoong.potatomallback.product.repository;

import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.product.dto.response.ProductCategorySearchResDto;

public interface ProductCategoryRepositoryCustom {

    PageResponseDto<ProductCategorySearchResDto> findProductCategoryWithPage(PageRequestDto pageRequestDto);
}
