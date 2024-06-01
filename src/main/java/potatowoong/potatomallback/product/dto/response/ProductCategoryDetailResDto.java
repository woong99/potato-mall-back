package potatowoong.potatomallback.product.dto.response;

import lombok.Builder;
import potatowoong.potatomallback.product.entity.ProductCategory;

@Builder
public record ProductCategoryDetailResDto(
    Long productCategoryId,
    String name
) {

    public static ProductCategoryDetailResDto of(ProductCategory productCategory) {
        return ProductCategoryDetailResDto.builder()
            .productCategoryId(productCategory.getProductCategoryId())
            .name(productCategory.getName())
            .build();
    }
}
