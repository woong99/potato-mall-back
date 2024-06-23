package potatowoong.potatomallback.domain.product.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.entity.ProductLike;
import potatowoong.potatomallback.domain.product.repository.ProductLikeRepository;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class ProductLikeServiceTest {

    @Mock
    private ProductLikeRepository productLikeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductLikeService productLikeService;

    @Nested
    @DisplayName("상품 좋아요 추가")
    class 상품_좋아요_추가 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            Product product = Product.builder().build();

            given(productLikeRepository.existsByProductProductIdAndMemberUserId(anyLong(), any())).willReturn(false);
            given(productRepository.findById(any())).willReturn(Optional.of(product));
            given(memberRepository.getReferenceById(any())).willReturn(any());

            // when
            productLikeService.addProductLike(anyLong());

            // then
            then(productLikeRepository).should().existsByProductProductIdAndMemberUserId(anyLong(), any());
            then(productRepository).should().findById(any());
            then(memberRepository).should().getReferenceById(any());
            then(productLikeRepository).should().save(any());
        }

        @Test
        @DisplayName("실패 - 이미 좋아요한 상품")
        void 실패_이미_좋아요한_상품() {
            // given
            given(productLikeRepository.existsByProductProductIdAndMemberUserId(anyLong(), anyString())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> productLikeService.addProductLike(1))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_LIKED_PRODUCT);

            then(productLikeRepository).should().existsByProductProductIdAndMemberUserId(anyLong(), anyString());
            then(productRepository).should(never()).findById(any());
            then(memberRepository).should(never()).getReferenceById(any());
            then(productLikeRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 - 상품 정보 없음")
        void 실패_상품_정보_없음() {
            // given
            given(productLikeRepository.existsByProductProductIdAndMemberUserId(anyLong(), anyString())).willReturn(false);
            given(productRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productLikeService.addProductLike(1))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

            then(productLikeRepository).should().existsByProductProductIdAndMemberUserId(anyLong(), anyString());
            then(productRepository).should().findById(any());
            then(memberRepository).should(never()).getReferenceById(any());
            then(productLikeRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("상품 좋아요 삭제")
    class 상품_좋아요_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            ProductLike productLike = ProductLike.builder().build();
            given(productLikeRepository.findByProductProductIdAndMemberUserId(anyLong(), any())).willReturn(Optional.of(productLike));

            // when
            productLikeService.removeProductLike(1);

            // then
            then(productLikeRepository).should().findByProductProductIdAndMemberUserId(anyLong(), any());
            then(productLikeRepository).should().delete(any());
        }

        @Test
        @DisplayName("실패 - 좋아요하지 않은 상품")
        void 실패_좋아요하지_않은_상품() {
            // given
            given(productLikeRepository.findByProductProductIdAndMemberUserId(anyLong(), any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productLikeService.removeProductLike(1))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_LIKED_PRODUCT);

            then(productLikeRepository).should().findByProductProductIdAndMemberUserId(anyLong(), any());
            then(productLikeRepository).should(never()).delete(any());
        }
    }
}