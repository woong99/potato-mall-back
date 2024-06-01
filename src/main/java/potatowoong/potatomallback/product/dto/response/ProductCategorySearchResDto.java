package potatowoong.potatomallback.product.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductCategorySearchResDto(
    // TODO : ProductCount 추가
    Long productCategoryId,
    String name,
    LocalDateTime updatedAt
) {

}
