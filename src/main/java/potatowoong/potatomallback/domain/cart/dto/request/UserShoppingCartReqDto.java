package potatowoong.potatomallback.domain.cart.dto.request;

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

        @NotNull
        Long productId,

        @NotNull
        Integer quantity
    ) {

    }

    /**
     * 장바구니 상품 수정 요청 DTO
     */
    @Builder
    public record Modify(

        @NotNull
        Long shoppingCartId,

        @NotNull
        Integer quantity
    ) {

    }
}
