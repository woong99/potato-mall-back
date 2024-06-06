package potatowoong.potatomallback.product.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import potatowoong.potatomallback.product.dto.response.ProductResDto.ProductRelatedResDto;
import potatowoong.potatomallback.product.entity.ProductCategory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductCategoryResDto {

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

    @Builder
    public record ProductCategorySearchResDto(
        long productCategoryId,
        String name,
        long productCount,
        LocalDateTime updatedAt
    ) {

    }
}
