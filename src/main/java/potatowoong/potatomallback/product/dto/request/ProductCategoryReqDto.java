package potatowoong.potatomallback.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductCategoryReqDto {

    @Builder
    public record ProductCategoryAddReqDto(

        @NotBlank(message = "카테고리명을 입력해주세요.")
        @Size(max = 100, message = "카테고리명은 100자 이하로 입력해주세요.")
        String name
    ) {

    }

    @Builder
    public record ProductCategoryModifyReqDto(

        @NotNull(message = "상품 카테고리 정보 ID는 필수 입력 값입니다.")
        Long productCategoryId,

        @NotBlank(message = "카테고리명을 입력해주세요.")
        @Size(max = 100, message = "카테고리명은 100자 이하로 입력해주세요.")
        String name
    ) {

    }
}
