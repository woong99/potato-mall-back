package potatowoong.potatomallback.domain.cart.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.cart.dto.request.UserShoppingCartReqDto;
import potatowoong.potatomallback.domain.cart.dto.response.UserShoppingCartResDto;
import potatowoong.potatomallback.domain.cart.dto.response.UserShoppingCartResDto.DetailWithProduct;
import potatowoong.potatomallback.domain.cart.service.UserShoppingCartService;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.common.ResponseText;

@RestController
@RequestMapping("/api/user/shopping-cart")
@RequiredArgsConstructor
public class UserShoppingCartController {

    private final UserShoppingCartService userShoppingCartService;

    /**
     * 자신의 장바구니 상품 개수 조회 API
     */
    @GetMapping("/me/items-count")
    public ResponseEntity<ApiResponseEntity<Integer>> getShoppingCartCount() {
        // 장바구니 상품 개수 조회
        int count = userShoppingCartService.getShoppingCartCount();

        return ResponseEntity.ok(ApiResponseEntity.of(count));
    }

    /**
     * 자신의 장바구니 상품 목록 조회 API
     */
    @GetMapping("/me/items")
    public ResponseEntity<ApiResponseEntity<List<UserShoppingCartResDto.DetailWithProduct>>> getShoppingCartList() {
        // 장바구니 상품 목록 조회
        List<DetailWithProduct> shoppingCartList = userShoppingCartService.getShoppingCartList();

        return ResponseEntity.ok(ApiResponseEntity.of(shoppingCartList));
    }

    /**
     * 장바구니 상품 상세 조회 API
     */
    @GetMapping("/{shoppingCartId}")
    public ResponseEntity<ApiResponseEntity<UserShoppingCartResDto.Detail>> getShoppingCart(@PathVariable long shoppingCartId) {
        // 장바구니 상품 상세 조회
        UserShoppingCartResDto.Detail shoppingCart = userShoppingCartService.getShoppingCart(shoppingCartId);

        return ResponseEntity.ok(ApiResponseEntity.of(shoppingCart));
    }

    /**
     * 장바구니 상품 등록 API
     */
    @PostMapping
    public ResponseEntity<ApiResponseEntity<String>> addShoppingCart(@Valid @RequestBody UserShoppingCartReqDto.Add dto) {
        // 장바구니 상품 등록
        userShoppingCartService.addShoppingCart(dto);

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_ADD_SHOPPING_CART));
    }

    /**
     * 장바구니 상품 수정 API
     */
    @PutMapping
    public ResponseEntity<ApiResponseEntity<String>> modifyShoppingCart(@Valid @RequestBody UserShoppingCartReqDto.Modify dto) {
        // 장바구니 상품 수정
        userShoppingCartService.modifyShoppingCart(dto);

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_MODIFY_SHOPPING_CART));
    }

    /**
     * 장바구니 상품 삭제 API
     */
    @DeleteMapping("/{shoppingCartId}")
    public ResponseEntity<ApiResponseEntity<String>> removeShoppingCart(@PathVariable long shoppingCartId) {
        // 장바구니 상품 삭제
        userShoppingCartService.removeShoppingCart(shoppingCartId);

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_REMOVE_SHOPPING_CART));
    }
}
