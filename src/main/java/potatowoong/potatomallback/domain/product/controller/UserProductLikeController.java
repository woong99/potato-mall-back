package potatowoong.potatomallback.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.product.service.ProductLikeService;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.common.ResponseText;

@RestController
@RequestMapping("/api/user/product")
@RequiredArgsConstructor
public class UserProductLikeController {

    private final ProductLikeService productLikeService;

    /**
     * 상품 좋아요 추가 API
     */
    @PostMapping("/{productId}/like")
    public ResponseEntity<ApiResponseEntity<String>> likeProduct(@PathVariable long productId) {
        // 상품 좋아요 추가
        productLikeService.addProductLike(productId);

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_ADD_PRODUCT_LIKE));
    }

    /**
     * 상품 좋아요 삭제 API
     */
    @DeleteMapping("/{productId}/like")
    public ResponseEntity<ApiResponseEntity<String>> unlikeProduct(@PathVariable long productId) {
        // 상품 좋아요 삭제
        productLikeService.removeProductLike(productId);

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_REMOVE_PRODUCT_LIKE));
    }
}
