package potatowoong.potatomallback.product.dto.response;

import lombok.Builder;
import potatowoong.potatomallback.product.document.ProductNameDocument;

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
