package potatowoong.potatomallback.domain.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.common.PageResponseDto;

@ExtendWith(MockitoExtension.class)
class UserProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UserProductService userProductService;

    @Nested
    @DisplayName("사용자 - 상품 목록 조회")
    class 사용자_상품_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(productRepository.findUserProductWithPage(any())).willReturn(new PageResponseDto<>(Collections.singletonList(UserProductResDto.Search.builder().build()), 1));

            // when
            PageResponseDto<UserProductResDto.Search> result = userProductService.getUserProductList(any());

            // then
            assertThat(result).isNotNull();
            then(productRepository).should().findUserProductWithPage(any());
        }
    }
}