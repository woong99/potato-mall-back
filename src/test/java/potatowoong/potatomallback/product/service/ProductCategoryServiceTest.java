package potatowoong.potatomallback.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.product.dto.request.ProductCategoryAddReqDto;
import potatowoong.potatomallback.product.dto.request.ProductCategoryModifyReqDto;
import potatowoong.potatomallback.product.dto.response.ProductCategoryDetailResDto;
import potatowoong.potatomallback.product.dto.response.ProductCategorySearchResDto;
import potatowoong.potatomallback.product.entity.ProductCategory;
import potatowoong.potatomallback.product.repository.ProductCategoryRepository;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceTest {

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @InjectMocks
    private ProductCategoryService productCategoryService;

    private final String categoryName = "카테고리명";

    private final long categoryId = 1L;

    @Nested
    @DisplayName("상품 카테고리 목록 조회")
    class 상품_카테고리_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(productCategoryRepository.findProductCategoryWithPage(any())).willReturn(new PageResponseDto<>(Arrays.asList(ProductCategorySearchResDto.builder().build()), 0L));

            // when
            PageResponseDto<ProductCategorySearchResDto> result = productCategoryService.getProductCategoryList(any());

            // then
            then(productCategoryRepository).should().findProductCategoryWithPage(any());
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("상품 카테고리 상세 조회")
    class 상품_카테고리_상세_조회 {

        // TODO : 연관된 상품 목록 추가 (테스트 코드 작성)

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(productCategoryRepository.findById(categoryId)).willReturn(Optional.of(createProductCategory()));

            // when
            ProductCategoryDetailResDto result = productCategoryService.getProductCategory(categoryId);

            // then
            then(productCategoryRepository).should().findById(categoryId);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리 ID")
        void 실패_존재하지_않는_카테고리_ID() {
            // given
            given(productCategoryRepository.findById(categoryId)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> productCategoryService.getProductCategory(categoryId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_CATEGORY);

            // then
            then(productCategoryRepository).should().findById(categoryId);
        }
    }

    @Nested
    @DisplayName("상품 카테고리 이름 조회")
    class 상품_카테고리_이름_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(productCategoryRepository.findById(categoryId)).willReturn(Optional.of(createProductCategory()));

            // when
            productCategoryService.getProductCategoryName(categoryId);

            // then
            then(productCategoryRepository).should().findById(categoryId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리 ID")
        void 실패_존재하지_않는_카테고리_ID() {
            // given
            given(productCategoryRepository.findById(categoryId)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> productCategoryService.getProductCategoryName(categoryId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_CATEGORY);

            // then
            then(productCategoryRepository).should().findById(categoryId);
        }
    }

    @Nested
    @DisplayName("상품 카테고리 저장")
    class 상품_카테고리_저장 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            ProductCategoryAddReqDto dto = createProductCategoryAddReqDto();

            given(productCategoryRepository.findByName(categoryName)).willReturn(Optional.empty());

            // when
            productCategoryService.addProductCategory(dto);

            // then
            then(productCategoryRepository).should().findByName(categoryName);
            then(productCategoryRepository).should().save(any(ProductCategory.class));
        }

        @Test
        @DisplayName("실패 - 중복된 카테고리명")
        void 실패_중복된_카테고리명() {
            // given
            ProductCategoryAddReqDto dto = createProductCategoryAddReqDto();

            given(productCategoryRepository.findByName(categoryName)).willReturn(Optional.of(ProductCategory.addOf(dto)));

            // when
            assertThatThrownBy(() -> productCategoryService.addProductCategory(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_CATEGORY_NAME);

            // then
            then(productCategoryRepository).should().findByName(categoryName);
        }

        private ProductCategoryAddReqDto createProductCategoryAddReqDto() {
            return ProductCategoryAddReqDto.builder()
                .name(categoryName)
                .build();
        }
    }

    @Nested
    @DisplayName("상품 카테고리 수정")
    class 상품_카테고리_수정 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            ProductCategoryModifyReqDto dto = createProductCategoryModifyReqDto();
            ProductCategory productCategory = createProductCategory();

            given(productCategoryRepository.findById(any())).willReturn(Optional.of(productCategory));

            // when
            productCategoryService.modifyProductCategory(dto);

            // then
            then(productCategoryRepository).should().findById(any());
            then(productCategoryRepository).should().findByName(categoryName);
            then(productCategoryRepository).should().save(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리 ID")
        void 실패_존재하지_않는_카테고리_ID() {
            // given
            ProductCategoryModifyReqDto dto = createProductCategoryModifyReqDto();

            given(productCategoryRepository.findById(any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> productCategoryService.modifyProductCategory(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_CATEGORY);

            // then
            then(productCategoryRepository).should().findById(any());
        }

        @Test
        @DisplayName("실패 - 중복된 카테고리명")
        void 실패_중복된_카테고리명() {
            // given
            ProductCategoryModifyReqDto dto = createProductCategoryModifyReqDto();
            ProductCategory productCategory = Mockito.spy(createProductCategory());

            given(productCategory.getProductCategoryId()).willReturn(2L);
            given(productCategoryRepository.findById(any())).willReturn(Optional.of(productCategory));
            given(productCategoryRepository.findByName(categoryName)).willReturn(Optional.of(productCategory));

            // when
            assertThatThrownBy(() -> productCategoryService.modifyProductCategory(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_CATEGORY_NAME);

            // then
            then(productCategoryRepository).should().findById(any());
            then(productCategoryRepository).should().findByName(categoryName);
        }

        private ProductCategoryModifyReqDto createProductCategoryModifyReqDto() {
            return ProductCategoryModifyReqDto.builder()
                .productCategoryId(categoryId)
                .name(categoryName)
                .build();
        }
    }

    @Nested
    @DisplayName("상품 카테고리 삭제")
    class 상품_카테고리_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            ProductCategory productCategory = createProductCategory();

            given(productCategoryRepository.findById(any())).willReturn(Optional.of(productCategory));

            // when
            productCategoryService.removeProductCategory(categoryId);

            // then
            then(productCategoryRepository).should().findById(any());
            then(productCategoryRepository).should().save(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리 ID")
        void 실패_존재하지_않는_카테고리_ID() {
            // given
            given(productCategoryRepository.findById(any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> productCategoryService.removeProductCategory(categoryId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_CATEGORY);

            // then
            then(productCategoryRepository).should().findById(any());
        }

        @Test
        @DisplayName("실패 - 카테고리에 속한 상품이 존재하는 경우")
        void 실패_카테고리에_속한_상품이_존재하는_경우() {
            // TODO : 상품에 대한 개발 후 테스트 코드 작성
        }
    }

    private ProductCategory createProductCategory() {
        return ProductCategory.builder()
            .name(categoryName)
            .build();
    }
}