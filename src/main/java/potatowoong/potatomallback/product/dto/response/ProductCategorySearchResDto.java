package potatowoong.potatomallback.product.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductCategorySearchResDto(
    long productCategoryId,
    String name,
    long productCount,
    LocalDateTime updatedAt
) {

}
