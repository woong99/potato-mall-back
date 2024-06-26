package potatowoong.potatomallback.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import potatowoong.potatomallback.domain.auth.entity.Member;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.domain.review.dto.request.UserReviewReqDto;
import potatowoong.potatomallback.domain.review.dto.request.UserReviewReqDto.Search;
import potatowoong.potatomallback.domain.review.dto.response.UserReviewResDto;
import potatowoong.potatomallback.domain.review.entity.Review;
import potatowoong.potatomallback.domain.review.repository.ReviewRepository;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private UserReviewService userReviewService;

    @Nested
    @DisplayName("리뷰 목록 조회")
    class 리뷰_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            UserReviewReqDto.Search dto = new Search("", null, 10, 1, null, null, 1L);
            given(reviewRepository.findReviewWithPage(any())).willReturn(new PageResponseDto<>(Collections.singletonList(UserReviewResDto.Detail.builder().build()), 0L));
            given(reviewRepository.sumScoreByProductId(anyLong())).willReturn(5);

            // when
            UserReviewResDto.Search result = userReviewService.getReviewList(dto);

            // then
            assertThat(result).isNotNull();

            then(reviewRepository).should().findReviewWithPage(any());
        }
    }

    @Nested
    @DisplayName("리뷰 상세 조회")
    class 리뷰_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            Review review = Mockito.spy(getReview());

            given(review.getCreatedAt()).willReturn(LocalDateTime.now());
            given(reviewRepository.findByReviewIdAndMemberUserId(anyLong(), any())).willReturn(Optional.of(review));

            // when
            UserReviewResDto.Detail result = userReviewService.getReview(1L);

            // then
            assertThat(result).isNotNull();

            then(reviewRepository).should().findByReviewIdAndMemberUserId(anyLong(), any());
        }

        @Test
        @DisplayName("실패 - 리뷰 정보가 없는 경우")
        void 실패_리뷰정보가_없는_경우() {
            // given
            given(reviewRepository.findByReviewIdAndMemberUserId(anyLong(), any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userReviewService.getReview(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);

            then(reviewRepository).should().findByReviewIdAndMemberUserId(anyLong(), any());
        }

        private Review getReview() {
            return Review.builder()
                .member(Member.builder()
                    .nickname("닉네임")
                    .build())
                .build();
        }
    }

    @Nested
    @DisplayName("리뷰 등록")
    class 리뷰_등록 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(reviewRepository.existsByProductProductIdAndMemberUserId(eq(productId), any())).willReturn(false);
            given(memberRepository.getReferenceById(any())).willReturn(any());
            // TODO : 결제 내역 조회

            // when
            userReviewService.addReview(dto);

            // then
            then(productRepository).should().findById(productId);
            then(reviewRepository).should().existsByProductProductIdAndMemberUserId(eq(productId), any());
            then(memberRepository).should().getReferenceById(any());
            // TODO : 결제 내역 조회
            then(reviewRepository).should().save(any());

        }

        @Test
        @DisplayName("실패 - 상품 정보가 없는 경우")
        void 실패_상품정보가_없는_경우() {
            // given
            given(productRepository.findById(productId)).willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> userReviewService.addReview(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

            then(productRepository).should().findById(productId);
            then(reviewRepository).should(never()).existsByProductProductIdAndMemberUserId(eq(productId), any());
            then(memberRepository).should(never()).getReferenceById(any());
            // TODO : 결제 내역 조회
            then(reviewRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 - 이미 리뷰를 작성한 경우")
        void 실패_이미_리뷰를_작성한_경우() {
            // given
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(reviewRepository.existsByProductProductIdAndMemberUserId(eq(productId), any())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userReviewService.addReview(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_ALREADY_EXISTS);

            then(productRepository).should().findById(any());
            then(reviewRepository).should().existsByProductProductIdAndMemberUserId(eq(productId), any());
            then(memberRepository).should(never()).getReferenceById(any());
            // TODO : 결제 내역 조회
            then(reviewRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 - 구매하지 않은 상품에 대한 리뷰 작성 시도")
        void 실패_구매하지_않은_상품에_대한_리뷰_작성_시도() {
            // TODO : 결제 기능 구현 후 작성
        }

        private final long productId = 1L;

        private final Product product = Product.builder().build();

        private final UserReviewReqDto.Add dto = UserReviewReqDto.Add.builder()
            .productId(productId)
            .contents("리뷰 내용")
            .score(5)
            .build();
    }

    @Nested
    @DisplayName("리뷰 수정")
    class 리뷰_수정 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(reviewRepository.findByReviewIdAndMemberUserId(anyLong(), any())).willReturn(Optional.of(review));

            // when
            userReviewService.modifyReview(dto);

            // then
            then(reviewRepository).should().findByReviewIdAndMemberUserId(anyLong(), any());
            then(reviewRepository).should().save(any());
        }

        @Test
        @DisplayName("실패 - 리뷰 정보가 없는 경우")
        void 실패_리뷰정보가_없는_경우() {
            // given
            given(reviewRepository.findByReviewIdAndMemberUserId(anyLong(), any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userReviewService.modifyReview(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);

            then(reviewRepository).should().findByReviewIdAndMemberUserId(anyLong(), any());
            then(reviewRepository).should(never()).save(any());
        }

        private final Review review = Review.builder().build();

        private final UserReviewReqDto.Modify dto = UserReviewReqDto.Modify.builder()
            .reviewId(1L)
            .contents("리뷰 내용 수정")
            .score(4)
            .build();
    }

    @Nested
    @DisplayName("리뷰 삭제")
    class 리뷰_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(reviewRepository.findByReviewIdAndMemberUserId(anyLong(), any())).willReturn(Optional.of(review));

            // when
            userReviewService.removeReview(1L);

            // then
            then(reviewRepository).should().findByReviewIdAndMemberUserId(anyLong(), any());
            then(reviewRepository).should().delete(any());
        }

        @Test
        @DisplayName("실패 - 리뷰 정보가 없는 경우")
        void 실패_리뷰정보가_없는_경우() {
            // given
            given(reviewRepository.findByReviewIdAndMemberUserId(anyLong(), any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userReviewService.removeReview(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);

            then(reviewRepository).should().findByReviewIdAndMemberUserId(anyLong(), any());
            then(reviewRepository).should(never()).delete(any());
        }

        private final Review review = Review.builder().build();
    }
}