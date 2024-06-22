package potatowoong.potatomallback.domain.product.dto.response;

import lombok.Builder;
import potatowoong.potatomallback.domain.product.document.ProductNameDocument;

@Builder
public record ProductNameResDto(
    String name
) {

    public static ProductNameResDto of(ProductNameDocument productNameDocument) {
        return ProductNameResDto.builder()
            .name(productNameDocument.getName())
            .build();
    }
}
