package potatowoong.potatomallback.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.auth.entity.Member;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.domain.product.dto.response.UserProductLikeResDto;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.entity.ProductLike;
import potatowoong.potatomallback.domain.product.repository.ProductLikeRepository;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.global.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;

    private final ProductRepository productRepository;

    private final MemberRepository memberRepository;

    /**
     * 상품 좋아요 추가
     */
    @Transactional
    public UserProductLikeResDto addProductLike(long productId) {
        final String userId = SecurityUtils.getCurrentUserId();

        // 이미 좋아요한 상품인지 확인
        if (productLikeRepository.existsByProductProductIdAndMemberUserId(productId, userId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED_PRODUCT);
        }

        // 상품 정보 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 사용자 정보 조회
        Member member = memberRepository.getReferenceById(userId);

        // 상품 좋아요 추가
        ProductLike productLike = ProductLike.builder()
            .product(product)
            .member(member)
            .build();
        productLikeRepository.save(productLike);

        // 상품 좋아요 개수 조회
        return getProductLikeCount(productId, userId);
    }

    /**
     * 상품 좋아요 삭제
     */
    @Transactional
    public UserProductLikeResDto removeProductLike(long productId) {
        final String userId = SecurityUtils.getCurrentUserId();

        // 좋아요한 상품인지 확인
        ProductLike productLike = productLikeRepository.findByProductProductIdAndMemberUserId(productId, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_LIKED_PRODUCT));

        // 상품 좋아요 삭제
        productLikeRepository.delete(productLike);

        // 상품 좋아요 개수 조회
        return getProductLikeCount(productId, userId);
    }

    /**
     * 상품 좋아요 개수 조회
     */
    private UserProductLikeResDto getProductLikeCount(final long productId, final String userId) {
        final int likeCount = productLikeRepository.countByProductProductIdAndMemberUserId(productId, userId);
        return new UserProductLikeResDto(likeCount);
    }
}
