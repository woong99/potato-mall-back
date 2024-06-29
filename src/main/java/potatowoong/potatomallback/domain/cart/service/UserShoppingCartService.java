package potatowoong.potatomallback.domain.cart.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.auth.entity.Member;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.domain.cart.dto.request.UserShoppingCartReqDto;
import potatowoong.potatomallback.domain.cart.dto.response.UserShoppingCartResDto;
import potatowoong.potatomallback.domain.cart.entity.ShoppingCart;
import potatowoong.potatomallback.domain.cart.repository.ShoppingCartRepository;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.global.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
public class UserShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;

    private final ProductRepository productRepository;

    private final MemberRepository memberRepository;

    /**
     * 자신의 장바구니 상품 개수 조회
     */
    @Transactional(readOnly = true)
    public int getShoppingCartCount() {
        return shoppingCartRepository.countByMemberUserId(SecurityUtils.getCurrentUserId());
    }

    /**
     * 자신의 장바구니 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserShoppingCartResDto.DetailWithProduct> getShoppingCartList() {
        return shoppingCartRepository.findAllByMemberUserId(SecurityUtils.getCurrentUserId()).stream()
            .map(UserShoppingCartResDto.DetailWithProduct::of)
            .toList();
    }

    /**
     * 장바구니 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public UserShoppingCartResDto.Detail getShoppingCart(final long shoppingCartId) {
        return shoppingCartRepository.findByShoppingCartIdAndMemberUserId(shoppingCartId, SecurityUtils.getCurrentUserId())
            .map(UserShoppingCartResDto.Detail::of)
            .orElseThrow(() -> new CustomException(ErrorCode.SHOPPING_CART_NOT_FOUND));
    }

    /**
     * 장바구니 상품 등록
     */
    @Transactional
    public void addShoppingCart(UserShoppingCartReqDto.Add dto) {
        // 상품 조회
        Product product = productRepository.findById(dto.productId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 재고량 확인
        if (dto.quantity() > product.getStockQuantity()) {
            throw new CustomException(ErrorCode.PRODUCT_QUANTITY_EXCEEDED);
        }

        // 사용자 정보 조회
        Member member = memberRepository.getReferenceById(SecurityUtils.getCurrentUserId());

        // 장바구니 등록
        ShoppingCart shoppingCart = ShoppingCart.builder()
            .product(product)
            .member(member)
            .quantity(dto.quantity())
            .build();
        shoppingCartRepository.save(shoppingCart);
    }

    /**
     * 장바구니 상품 수정
     */
    @Transactional
    public void modifyShoppingCart(UserShoppingCartReqDto.Modify dto) {
        // 장바구니 조회
        ShoppingCart shoppingCart = shoppingCartRepository.findByShoppingCartIdAndMemberUserId(dto.shoppingCartId(), SecurityUtils.getCurrentUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.SHOPPING_CART_NOT_FOUND));

        // 재고량 확인
        if (dto.quantity() > shoppingCart.getProduct().getStockQuantity()) {
            throw new CustomException(ErrorCode.PRODUCT_QUANTITY_EXCEEDED);
        }

        // 장바구니 수정
        shoppingCart.modifyShoppingCart(dto.quantity());
        shoppingCartRepository.save(shoppingCart);
    }

    /**
     * 장바구니 상품 삭제
     */
    @Transactional
    public void removeShoppingCart(final long shoppingCartId) {
        // 장바구니 조회
        ShoppingCart shoppingCart = shoppingCartRepository.findByShoppingCartIdAndMemberUserId(shoppingCartId, SecurityUtils.getCurrentUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.SHOPPING_CART_NOT_FOUND));

        // 장바구니 삭제
        shoppingCartRepository.delete(shoppingCart);
    }
}
