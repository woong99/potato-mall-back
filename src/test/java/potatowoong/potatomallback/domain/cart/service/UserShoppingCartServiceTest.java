package potatowoong.potatomallback.domain.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.domain.cart.dto.request.UserShoppingCartReqDto;
import potatowoong.potatomallback.domain.cart.dto.response.UserShoppingCartResDto;
import potatowoong.potatomallback.domain.cart.entity.ShoppingCart;
import potatowoong.potatomallback.domain.cart.repository.ShoppingCartRepository;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserShoppingCartServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @InjectMocks
    private UserShoppingCartService userShoppingCartService;

    @Nested
    @DisplayName("자신의 장바구니 상품 개수 조회")
    class 자신의_장바구니_상품_개수_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(shoppingCartRepository.countByMemberUserId(anyString())).willReturn(1);

            // when
            int count = userShoppingCartService.getShoppingCartCount();

            // then
            then(shoppingCartRepository).should().countByMemberUserId(anyString());
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("자신의 장바구니 상품 목록 조회")
    class 자신의_장바구니_상품_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            ShoppingCart shoppingCart = Mockito.spy(getShoppingCart());

            given(shoppingCart.getShoppingCartId()).willReturn(1L);
            given(shoppingCartRepository.findAllByMemberUserId(anyString())).willReturn(Collections.singletonList(shoppingCart));

            // when
            List<UserShoppingCartResDto.DetailWithProduct> shoppingCartList = userShoppingCartService.getShoppingCartList();

            // then
            then(shoppingCartRepository).should().findAllByMemberUserId(anyString());
            assertThat(shoppingCartList).hasSize(1);
        }

        private ShoppingCart getShoppingCart() {
            return ShoppingCart.builder()
                .product(Product.builder().build())
                .build();
        }
    }

    @Nested
    @DisplayName("장바구니 상품 상세 조회")
    class 장바구니_상품_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            ShoppingCart shoppingCart = Mockito.spy(getShoppingCart());
            given(shoppingCart.getShoppingCartId()).willReturn(1L);
            given(shoppingCartRepository.findByShoppingCartIdAndMemberUserId(anyLong(), anyString())).willReturn(Optional.of(shoppingCart));

            // when
            UserShoppingCartResDto.Detail result = userShoppingCartService.getShoppingCart(1L);

            // then
            then(shoppingCartRepository).should().findByShoppingCartIdAndMemberUserId(anyLong(), anyString());
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 장바구니 상품")
        void 실패_존재하지_않는_장바구니() {
            // given
            given(shoppingCartRepository.findByShoppingCartIdAndMemberUserId(anyLong(), anyString())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userShoppingCartService.getShoppingCart(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOPPING_CART_NOT_FOUND);

            then(shoppingCartRepository).should().findByShoppingCartIdAndMemberUserId(anyLong(), anyString());
        }

        private ShoppingCart getShoppingCart() {
            return ShoppingCart.builder()
                .product(Product.builder().build())
                .build();
        }
    }

    @Nested
    @DisplayName("장바구니 상품 등록")
    class 등록 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(productRepository.findById(any())).willReturn(Optional.of(product));
            given(memberRepository.getReferenceById(any())).willReturn(any());

            // when
            userShoppingCartService.addShoppingCart(dto);

            // then
            then(productRepository).should().findById(any());
            then(memberRepository).should().getReferenceById(any());
            then(shoppingCartRepository).should().save(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품")
        void 실패_존재하지_않는_상품() {
            // given
            given(productRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userShoppingCartService.addShoppingCart(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

            then(productRepository).should().findById(any());
            then(memberRepository).should(never()).getReferenceById(any());
            then(shoppingCartRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 - 상품 재고량 초과")
        void 싶패_매진된_상품() {
            // given
            given(productRepository.findById(any())).willReturn(Optional.of(product));

            // when & then
            assertThatThrownBy(() -> userShoppingCartService.addShoppingCart(wrongDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_QUANTITY_EXCEEDED);

            then(productRepository).should().findById(any());
            then(memberRepository).should(never()).getReferenceById(any());
            then(shoppingCartRepository).should(never()).save(any());
        }

        private final Product product = Product.builder()
            .stockQuantity(1)
            .build();

        private final UserShoppingCartReqDto.Add dto = UserShoppingCartReqDto.Add.builder()
            .productId(1L)
            .quantity(1)
            .build();

        private final UserShoppingCartReqDto.Add wrongDto = UserShoppingCartReqDto.Add.builder()
            .productId(1L)
            .quantity(2)
            .build();
    }

    @Nested
    @DisplayName("장바구니 상품 수정")
    class 수정 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(shoppingCartRepository.findByShoppingCartIdAndMemberUserId(anyLong(), anyString())).willReturn(Optional.of(shoppingCart));

            // when
            userShoppingCartService.modifyShoppingCart(dto);

            // then
            then(shoppingCartRepository).should().findByShoppingCartIdAndMemberUserId(anyLong(), anyString());
            then(shoppingCartRepository).should().save(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 장바구니 상품")
        void 실패_존재하지_않는_장바구니() {
            // given
            given(shoppingCartRepository.findByShoppingCartIdAndMemberUserId(anyLong(), anyString())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> userShoppingCartService.modifyShoppingCart(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOPPING_CART_NOT_FOUND);

            then(shoppingCartRepository).should().findByShoppingCartIdAndMemberUserId(anyLong(), anyString());
            then(shoppingCartRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 - 상품 재고량 초과")
        void 실패_매진된_상품() {
            // given
            given(shoppingCartRepository.findByShoppingCartIdAndMemberUserId(anyLong(), anyString())).willReturn(Optional.of(shoppingCart));

            // when
            assertThatThrownBy(() -> userShoppingCartService.modifyShoppingCart(wrongDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_QUANTITY_EXCEEDED);

            then(shoppingCartRepository).should().findByShoppingCartIdAndMemberUserId(anyLong(), anyString());
            then(shoppingCartRepository).should(never()).save(any());
        }

        private final ShoppingCart shoppingCart = ShoppingCart.builder()
            .product(Product.builder().stockQuantity(1).build())
            .quantity(1)
            .build();

        private final UserShoppingCartReqDto.Modify dto = UserShoppingCartReqDto.Modify.builder()
            .shoppingCartId(1L)
            .quantity(1)
            .build();

        private final UserShoppingCartReqDto.Modify wrongDto = UserShoppingCartReqDto.Modify.builder()
            .shoppingCartId(1L)
            .quantity(2)
            .build();
    }

    @Nested
    @DisplayName("장바구니 상품 삭제")
    class 삭제 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(shoppingCartRepository.findByShoppingCartIdAndMemberUserId(anyLong(), anyString())).willReturn(Optional.of(shoppingCart));

            // when
            userShoppingCartService.removeShoppingCart(1L);

            // then
            then(shoppingCartRepository).should().findByShoppingCartIdAndMemberUserId(anyLong(), anyString());
            then(shoppingCartRepository).should().delete(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 장바구니 상품")
        void 실패_존재하지_않는_장바구니() {
            // given
            given(shoppingCartRepository.findByShoppingCartIdAndMemberUserId(anyLong(), anyString())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> userShoppingCartService.removeShoppingCart(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOPPING_CART_NOT_FOUND);

            then(shoppingCartRepository).should().findByShoppingCartIdAndMemberUserId(anyLong(), anyString());
            then(shoppingCartRepository).should(never()).delete(any());
        }

        private final ShoppingCart shoppingCart = ShoppingCart.builder()
            .product(Product.builder().stockQuantity(1).build())
            .quantity(1)
            .build();
    }
}