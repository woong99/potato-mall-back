package potatowoong.potatomallback.product.dto.response;

import java.util.List;
import lombok.Builder;
import potatowoong.potatomallback.product.dto.response.ProductResDto.ProductRelatedResDto;
import potatowoong.potatomallback.product.entity.ProductCategory;

@Builder
public record ProductCategoryDetailResDto(
    Long productCategoryId,
    String name,
    List<ProductRelatedResDto> products
) {

    public static ProductCategoryDetailResDto of(ProductCategory productCategory) {
        return ProductCategoryDetailResDto.builder()
            .productCategoryId(productCategory.getProductCategoryId())
            .name(productCategory.getName())
            .products(productCategory.getProducts().stream()
                .map(ProductRelatedResDto::of)
                .toList())
            .build();
    }
}
