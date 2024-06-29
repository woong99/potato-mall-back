package potatowoong.potatomallback.domain.cart.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import potatowoong.potatomallback.domain.cart.entity.ShoppingCart;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserShoppingCartResDto {

    /**
     * 장바구니 정보 + 상품 정보 응답 DTO
     */
    @Builder
    public record DetailWithProduct(

        UserProductResDto.CartProduct product,

        long shoppingCartId,

        int quantity
    ) {

        public static DetailWithProduct of(ShoppingCart shoppingCart) {
            return DetailWithProduct.builder()
                .product(UserProductResDto.CartProduct.of(shoppingCart.getProduct()))
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .quantity(shoppingCart.getQuantity())
                .build();
        }
    }

    /**
     * 장바구니 정보 응답 DTO
     */
    @Builder
    public record Detail(

        long shoppingCartId,

        int quantity
    ) {

        public static Detail of(ShoppingCart shoppingCart) {
            return Detail.builder()
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .quantity(shoppingCart.getQuantity())
                .build();
        }
    }
}
