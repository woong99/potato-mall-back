package potatowoong.potatomallback.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.product.dto.response.UserProductLikeResDto;
import potatowoong.potatomallback.domain.product.service.ProductLikeService;
import potatowoong.potatomallback.global.common.ApiResponseEntity;

@RestController
@RequestMapping("/api/user/product")
@RequiredArgsConstructor
public class UserProductLikeController {

    private final ProductLikeService productLikeService;

    /**
     * 상품 좋아요 추가 API
     */
    @PostMapping("/{productId}/like")
    public ResponseEntity<ApiResponseEntity<UserProductLikeResDto>> likeProduct(@PathVariable long productId) {
        return ResponseEntity.ok(ApiResponseEntity.of(productLikeService.addProductLike(productId)));
    }

    /**
     * 상품 좋아요 삭제 API
     */
    @DeleteMapping("/{productId}/like")
    public ResponseEntity<ApiResponseEntity<UserProductLikeResDto>> unlikeProduct(@PathVariable long productId) {
        return ResponseEntity.ok(ApiResponseEntity.of(productLikeService.removeProductLike(productId)));
    }
}
