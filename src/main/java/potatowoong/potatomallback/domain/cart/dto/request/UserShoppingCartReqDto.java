package potatowoong.potatomallback.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserShoppingCartReqDto {

    /**
     * 장바구니 상품 추가 요청 DTO
     */
    @Builder
    public record Add(

        @NotNull(message = "상품 ID를 입력해주세요.")
        Long productId,

        @NotNull(message = "수량을 입력해주세요.")
        @Min(value = 1, message = "최소 1개 이상의 상품을 담아주세요.")
        Integer quantity
    ) {

    }

    /**
     * 장바구니 상품 수정 요청 DTO
     */
    @Builder
    public record Modify(

        @NotNull(message = "장바구니 ID를 입력해주세요.")
        Long shoppingCartId,

        @NotNull(message = "수량을 입력해주세요.")
        @Min(value = 1, message = "최소 1개 이상의 상품을 담아주세요.")
        Integer quantity
    ) {

    }
}
