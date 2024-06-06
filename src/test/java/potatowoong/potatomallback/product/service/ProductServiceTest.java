package potatowoong.potatomallback.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

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
import org.springframework.mock.web.MockMultipartFile;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.file.entity.AtchFile;
import potatowoong.potatomallback.file.enums.S3Folder;
import potatowoong.potatomallback.file.service.FileService;
import potatowoong.potatomallback.product.dto.request.ProductReqDto.ProductAddReqDto;
import potatowoong.potatomallback.product.dto.request.ProductReqDto.ProductModifyReqDto;
import potatowoong.potatomallback.product.dto.response.ProductResDto.ProductDetailResDto;
import potatowoong.potatomallback.product.dto.response.ProductResDto.ProductSearchResDto;
import potatowoong.potatomallback.product.entity.Product;
import potatowoong.potatomallback.product.entity.ProductCategory;
import potatowoong.potatomallback.product.repository.ProductCategoryRepository;
import potatowoong.potatomallback.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("상품 목록 조회")
    class 상품_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(productRepository.findProductWithPage(any())).willReturn(new PageResponseDto<>(Collections.singletonList(ProductSearchResDto.builder().build()), 1));

            // when
            PageResponseDto<ProductSearchResDto> result = productService.getProductList(any());

            // then
            assertThat(result).isNotNull();
            then(productRepository).should().findProductWithPage(any());
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class 상품_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            ProductCategory productCategory = ProductCategory.builder()
                .name("카테고리")
                .build();

            AtchFile atchFile = Mockito.spy(AtchFile.class);
            given(atchFile.getAtchFileId()).willReturn(1L);

            Product product = Product.builder()
                .name("상품")
                .price(10000)
                .stockQuantity(100)
                .productCategory(productCategory)
                .thumbnailFile(atchFile)
                .build();

            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.of(product));

            // when
            ProductDetailResDto result = productService.getProduct(1L);

            // then
            assertThat(result).isNotNull();
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품 ID")
        void 실패_존재하지_않는_상품_ID() {
            // given
            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> productService.getProduct(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

            // then
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
        }
    }

    @Nested
    @DisplayName("상품 등록")
    class 상품_등록 {

        @Test
        @DisplayName("성공 - 썸네일 이미지 O")
        void 성공_썸네일_이미지_O() {
            // given
            ProductAddReqDto productAddReqDto = ProductAddReqDto.builder()
                .name("상품")
                .price(10000)
                .stockQuantity(100)
                .productCategoryId(1L)
                .build();
            MockMultipartFile thumbnailFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            given(productCategoryRepository.findById(1L)).willReturn(Optional.of(ProductCategory.builder().build()));

            // when
            productService.addProduct(productAddReqDto, thumbnailFile);

            // then
            then(productCategoryRepository).should().findById(1L);
            then(productRepository).should().save(any());
            then(fileService).should().saveImageAtchFile(S3Folder.PRODUCT, thumbnailFile);
        }

        @Test
        @DisplayName("성공 - 썸네일 이미지 X")
        void 성공_썸네일_이미지_X() {
            // given
            ProductAddReqDto productAddReqDto = ProductAddReqDto.builder()
                .name("상품")
                .price(10000)
                .stockQuantity(100)
                .productCategoryId(1L)
                .build();

            given(productCategoryRepository.findById(1L)).willReturn(Optional.of(ProductCategory.builder().build()));

            // when
            productService.addProduct(productAddReqDto, null);

            // then
            then(productCategoryRepository).should().findById(1L);
            then(productRepository).should().save(any());
            then(fileService).should(never()).saveImageAtchFile(S3Folder.PRODUCT, null);
        }

        @Test
        @DisplayName("실패 - 잘못된 상품 카테고리 ID")
        void 실패_잘못된_상품_카테고리_ID() {
            // given
            ProductAddReqDto productAddReqDto = ProductAddReqDto.builder()
                .name("상품")
                .price(10000)
                .stockQuantity(100)
                .productCategoryId(1L)
                .build();
            MockMultipartFile thumbnailFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            willThrow(new CustomException(ErrorCode.NOT_FOUND_CATEGORY)).given(productCategoryRepository).findById(1L);

            // when
            assertThatThrownBy(() -> productService.addProduct(productAddReqDto, thumbnailFile))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_CATEGORY);

            // then
            then(productCategoryRepository).should().findById(1L);
            then(productRepository).should(never()).save(any());
            then(fileService).should(never()).saveImageAtchFile(any(), any());
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class 상품_수정 {

        @Test
        @DisplayName("성공 - 기존 썸네일이 있는데 새로운 썸네일로 변경")
        void 성공_기존_썸네일이_있는데_새로운_썸네일로_변경() {
            // given
            ProductModifyReqDto productModifyReqDto = ProductModifyReqDto.builder()
                .productId(1L)
                .name("상품")
                .price(10000)
                .stockQuantity(100)
                .productCategoryId(1L)
                .build();

            AtchFile atchFile = Mockito.spy(AtchFile.class);
            given(atchFile.getAtchFileId()).willReturn(1L);

            Product product = Product.builder()
                .thumbnailFile(atchFile)
                .build();

            MockMultipartFile thumbnailFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.of(product));
            given(productCategoryRepository.findById(1L)).willReturn(Optional.of(ProductCategory.builder().build()));

            // when
            productService.modifyProduct(productModifyReqDto, thumbnailFile);

            // then
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
            then(productCategoryRepository).should().findById(1L);
            then(productRepository).should().save(any());
            then(fileService).should().saveImageAtchFile(S3Folder.PRODUCT, thumbnailFile);
            then(fileService).should().removeAtchFile(1L);

        }

        @Test
        @DisplayName("성공 - 썸네일을 삭제하는 경우")
        void 성공_썸네일을_삭제하는_경우() {
            // given
            ProductModifyReqDto productModifyReqDto = ProductModifyReqDto.builder()
                .productId(1L)
                .name("상품")
                .price(10000)
                .stockQuantity(100)
                .productCategoryId(1L)
                .build();

            AtchFile atchFile = Mockito.spy(AtchFile.class);
            given(atchFile.getAtchFileId()).willReturn(1L);

            Product product = Product.builder()
                .thumbnailFile(atchFile)
                .build();

            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.of(product));
            given(productCategoryRepository.findById(1L)).willReturn(Optional.of(ProductCategory.builder().build()));

            // when
            productService.modifyProduct(productModifyReqDto, null);

            // then
            then(productCategoryRepository).should().findById(1L);
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
            then(productRepository).should().save(any());
            then(fileService).should(never()).saveImageAtchFile(S3Folder.PRODUCT, null);
            then(fileService).should().removeAtchFile(1L);
        }

        @Test
        @DisplayName("성공 - 기존 썸네일이 없고 새로운 썸네일을 추가하는 경우")
        void 성공_기존_썸네일이_없고_새로운_썸네일을_추가하는_경우() {
            // given
            ProductModifyReqDto productModifyReqDto = ProductModifyReqDto.builder()
                .productId(1L)
                .name("상품")
                .price(10000)
                .stockQuantity(100)
                .productCategoryId(1L)
                .build();

            Product product = Product.builder()
                .thumbnailFile(null)
                .build();

            MockMultipartFile thumbnailFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.of(product));
            given(productCategoryRepository.findById(1L)).willReturn(Optional.of(ProductCategory.builder().build()));

            // when
            productService.modifyProduct(productModifyReqDto, thumbnailFile);

            // then
            then(productCategoryRepository).should().findById(1L);
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
            then(productRepository).should().save(any());
            then(fileService).should().saveImageAtchFile(S3Folder.PRODUCT, thumbnailFile);
            then(fileService).should(never()).removeAtchFile(1L);
        }


        @Test
        @DisplayName("실패 - 존재하지 않는 상품 ID")
        void 실패_존재하지_않는_상품_ID() {
            // given
            ProductModifyReqDto productModifyReqDto = ProductModifyReqDto.builder()
                .productId(1L)
                .name("상품")
                .price(10000)
                .stockQuantity(100)
                .productCategoryId(1L)
                .build();

            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> productService.modifyProduct(productModifyReqDto, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

            // then
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
            then(productCategoryRepository).should(never()).findById(any());
            then(productRepository).should(never()).save(any());
            then(fileService).should(never()).saveImageAtchFile(any(), any());
            then(fileService).should(never()).removeAtchFile(any(Long.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 상품 카테고리 ID")
        void 실패_잘못된_상품_카테고리_ID() {
            // given
            ProductModifyReqDto productModifyReqDto = ProductModifyReqDto.builder()
                .productId(1L)
                .name("상품")
                .price(10000)
                .stockQuantity(100)
                .productCategoryId(1L)
                .build();

            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.of(Product.builder().build()));
            given(productCategoryRepository.findById(1L)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> productService.modifyProduct(productModifyReqDto, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_CATEGORY);

            // then
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
            then(productCategoryRepository).should().findById(1L);
            then(productRepository).should(never()).save(any());
            then(fileService).should(never()).saveImageAtchFile(any(), any());
            then(fileService).should(never()).removeAtchFile(any(Long.class));
        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class 상품_삭제 {

        @Test
        @DisplayName("성공 - 저장된 썸네일 이미지 O")
        void 성공_저장된_썸네일_이미지_O() {
            // given
            AtchFile atchFile = Mockito.spy(AtchFile.class);
            given(atchFile.getAtchFileId()).willReturn(1L);

            Product product = Product.builder()
                .thumbnailFile(atchFile)
                .build();

            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.of(product));

            // when
            productService.removeProduct(1L);

            // then
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
            then(productRepository).should().delete(any());
            then(fileService).should().removeAtchFile(any(Long.class));
        }

        @Test
        @DisplayName("성공 - 저장된 썸네일 이미지 X")
        void 성공_저장된_썸네일_이미지_X() {
            // given
            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.of(Product.builder().build()));

            // when
            productService.removeProduct(1L);

            // then
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
            then(productRepository).should().delete(any());
            then(fileService).should(never()).removeAtchFile(any(Long.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품 ID")
        void 실패_존재하지_않는_상품_ID() {
            // given
            given(productRepository.findWithThumbnailFileByProductId(1L)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> productService.removeProduct(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

            // then
            then(productRepository).should().findWithThumbnailFileByProductId(1L);
            then(productRepository).should(never()).save(any());
            then(fileService).should(never()).removeAtchFile(any(Long.class));
        }
    }
}