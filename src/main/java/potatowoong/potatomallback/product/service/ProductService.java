package potatowoong.potatomallback.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import potatowoong.potatomallback.common.PageRequestDto;
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
import potatowoong.potatomallback.utils.FileUtils;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final ProductCategoryRepository productCategoryRepository;

    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResponseDto<ProductSearchResDto> getProductList(PageRequestDto pageRequestDto) {
        return productRepository.findProductWithPage(pageRequestDto);
    }

    @Transactional(readOnly = true)
    public ProductDetailResDto getProduct(final long productId) {
        Product product = productRepository.findWithThumbnailFileByProductId(productId)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductDetailResDto.of(product);
    }

    @Transactional
    public void addProduct(ProductAddReqDto productAddReqDto, MultipartFile thumbnailFile) {
        // 상품 카테고리 조회
        ProductCategory productCategory = productCategoryRepository.findById(productAddReqDto.productCategoryId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY));

        // 썸네일 이미지 저장
        AtchFile thumbnail = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            thumbnail = fileService.saveImageAtchFile(S3Folder.PRODUCT, thumbnailFile);
        }

        // 상품 저장
        Product product = Product.addOf(productAddReqDto, productCategory, thumbnail);
        productRepository.save(product);
    }

    @Transactional
    public void modifyProduct(ProductModifyReqDto productModifyReqDto, MultipartFile thumbnailFile) {
        // 상품 조회
        Product product = productRepository.findWithThumbnailFileByProductId(productModifyReqDto.productId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 상품 카테고리 조회
        ProductCategory productCategory = productCategoryRepository.findById(productModifyReqDto.productCategoryId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY));

        // 썸네일 이미지 수정
        updateThumbnail(product.getThumbnailFile(), thumbnailFile, product, productModifyReqDto.thumbnailFileId());

        product.modify(productModifyReqDto, productCategory);
        productRepository.save(product);
    }

    @Transactional
    public void removeProduct(final long productId) {
        // 상품 조회
        Product product = productRepository.findWithThumbnailFileByProductId(productId)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 썸네일 이미지 삭제
        if (product.getThumbnailFile() != null) {
            fileService.removeAtchFile(product.getThumbnailFile().getAtchFileId());
        }

        // 상품 삭제
        productRepository.delete(product);
    }

    /**
     * 썸네일 이미지 수정
     */
    private void updateThumbnail(AtchFile savedThumbnail, MultipartFile thumbnailFile, Product product, Long thumbnailId) {
        if (savedThumbnail != null) {
            final long atchFileId = savedThumbnail.getAtchFileId();
            if (FileUtils.hasFile(thumbnailFile)) {
                // 기존 썸네일이 있는데 새로운 썸네일로 변경
                final AtchFile thumbnail = fileService.saveImageAtchFile(S3Folder.PRODUCT, thumbnailFile);
                product.changeThumbnailFile(thumbnail);

                fileService.removeAtchFile(atchFileId);
            } else if (thumbnailId == null && FileUtils.hasNotFile(thumbnailFile)) {
                // 기존 썸네일을 삭제하는 경우
                product.removeThumbnailFile();

                fileService.removeAtchFile(atchFileId);
            }
        } else if (FileUtils.hasFile(thumbnailFile)) {
            // 기존 썸네일이 없고 새로운 썸네일을 추가하는 경우
            final AtchFile thumbnail = fileService.saveImageAtchFile(S3Folder.PRODUCT, thumbnailFile);
            product.changeThumbnailFile(thumbnail);
        }
    }
}
