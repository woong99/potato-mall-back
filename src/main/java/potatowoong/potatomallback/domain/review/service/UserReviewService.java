package potatowoong.potatomallback.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.auth.entity.Member;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.domain.review.dto.request.UserReviewReqDto;
import potatowoong.potatomallback.domain.review.dto.response.UserReviewResDto;
import potatowoong.potatomallback.domain.review.dto.response.UserReviewResDto.Detail;
import potatowoong.potatomallback.domain.review.entity.Review;
import potatowoong.potatomallback.domain.review.repository.ReviewRepository;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.global.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
public class UserReviewService {

    private final ReviewRepository reviewRepository;

    private final ProductRepository productRepository;

    private final MemberRepository memberRepository;

    /**
     * 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public UserReviewResDto.Search getReviewList(UserReviewReqDto.Search dto) {
        // 리뷰 목록 조회
        PageResponseDto<UserReviewResDto.Detail> pageResponseDto = reviewRepository.findReviewWithPage(dto);

        // 상품의 총 평점 조회
        final int totalScore = reviewRepository.sumScoreByProductId(dto.getProductId());

        // 평균 평점 계산
        double averageScore = getAverageScore(totalScore, pageResponseDto.totalElements());

        return UserReviewResDto.Search.builder()
            .pageResponseDto(pageResponseDto)
            .averageScore(averageScore)
            .build();
    }

    /**
     * 리뷰 상세 조회
     */
    @Transactional(readOnly = true)
    public UserReviewResDto.Detail getReview(final long reviewId) {
        return reviewRepository.findByReviewIdAndMemberUserId(reviewId, SecurityUtils.getCurrentUserId())
            .map(Detail::of)
            .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    }

    /**
     * 리뷰 등록
     */
    @Transactional
    public void addReview(UserReviewReqDto.Add dto) {
        final String userId = SecurityUtils.getCurrentUserId();

        // 상품 정보 조회
        Product product = productRepository.findById(dto.productId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 리뷰 작성 유무 확인
        if (reviewRepository.existsByProductProductIdAndMemberUserId(dto.productId(), userId)) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 사용자 정보 조회
        Member member = memberRepository.getReferenceById(userId);

        // TODO : 상품 구매 유무 확인

        // 리뷰 등록
        Review review = Review.of(dto, product, member);
        reviewRepository.save(review);
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public void modifyReview(UserReviewReqDto.Modify dto) {
        // 리뷰 정보 조회
        Review review = reviewRepository.findByReviewIdAndMemberUserId(dto.reviewId(), SecurityUtils.getCurrentUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 리뷰 수정
        review.modifyReview(dto);
        reviewRepository.save(review);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void removeReview(final long reviewId) {
        // 리뷰 정보 조회
        Review review = reviewRepository.findByReviewIdAndMemberUserId(reviewId, SecurityUtils.getCurrentUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 리뷰 삭제
        reviewRepository.delete(review);
    }

    /**
     * 평균 평점 계산
     *
     * @param totalScore    총 평점
     * @param totalElements 총 리뷰 수
     * @return 평균 평점
     */
    private double getAverageScore(final int totalScore, final long totalElements) {
        double averageScore = 0;

        if (totalScore > 0 && totalElements > 0) {
            averageScore = (double) totalScore / totalElements;
        }

        return averageScore;
    }
}
