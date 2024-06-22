package potatowoong.potatomallback.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductReqDto {

    @Builder
    public record ProductAddReqDto(

        @NotBlank(message = "상품명을 입력해주세요.")
        @Size(max = 100, message = "상품명은 100자 이하로 입력해주세요.")
        String name,

        @NotBlank(message = "상품 설명을 입력해주세요.")
        String content,

        int price,

        int stockQuantity,

        @NotNull(message = "상품 카테고리를 선택해주세요.")
        Long productCategoryId
    ) {

    }

    @Builder
    public record ProductModifyReqDto(

        @NotNull(message = "상품 정보 ID를 입력해주세요.")
        Long productId,

        @NotBlank(message = "상품명을 입력해주세요.")
        @Size(max = 100, message = "상품명은 100자 이하로 입력해주세요.")
        String name,

        @NotBlank(message = "상품 설명을 입력해주세요.")
        String content,

        int price,

        int stockQuantity,

        @NotNull(message = "상품 카테고리를 선택해주세요.")
        Long productCategoryId,

        Long thumbnailFileId
    ) {

    }
}
