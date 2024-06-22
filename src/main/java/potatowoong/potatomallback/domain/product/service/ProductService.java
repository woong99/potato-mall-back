package potatowoong.potatomallback.domain.product.service;

import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import potatowoong.potatomallback.domain.product.document.ProductNameDocument;
import potatowoong.potatomallback.domain.product.dto.request.ProductReqDto.ProductAddReqDto;
import potatowoong.potatomallback.domain.product.dto.request.ProductReqDto.ProductModifyReqDto;
import potatowoong.potatomallback.domain.product.repository.ElasticProductNameRepository;
import potatowoong.potatomallback.domain.product.repository.ProductCategoryRepository;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.domain.file.entity.AtchFile;
import potatowoong.potatomallback.domain.file.enums.S3Folder;
import potatowoong.potatomallback.domain.file.service.FileService;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductDetailResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductSearchResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.UserProductSearchResDto;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.entity.ProductCategory;
import potatowoong.potatomallback.global.utils.FileUtils;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final ProductCategoryRepository productCategoryRepository;

    private final ElasticProductNameRepository elasticProductNameRepository;

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

    @Transactional(readOnly = true)
    public PageResponseDto<UserProductSearchResDto> getUserProductList(PageRequestDto pageRequestDto) {
        return productRepository.findUserProductWithPage(pageRequestDto);
    }

    @Transactional
    public void addProduct(ProductAddReqDto productAddReqDto, MultipartFile thumbnailFile) {
        // 상품명 중복 체크
        if (productRepository.findByName(productAddReqDto.name()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_PRODUCT_NAME);
        }

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

        // 상품명 Elastic Search에 저장
        insertProductNameToElasticSearch(productAddReqDto.name());
    }

    @Transactional
    public void modifyProduct(ProductModifyReqDto productModifyReqDto, MultipartFile thumbnailFile) {
        // 상품 조회
        Product product = productRepository.findWithThumbnailFileByProductId(productModifyReqDto.productId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 수정 전 상품명
        final String productName = product.getName();

        // 상품명 중복 체크
        Optional<Product> savedProduct = productRepository.findByName(productModifyReqDto.name());
        if (savedProduct.isPresent() && !Objects.equals(savedProduct.get().getProductId(), productModifyReqDto.productId())) {
            throw new CustomException(ErrorCode.DUPLICATED_PRODUCT_NAME);
        }

        // 상품 카테고리 조회
        ProductCategory productCategory = productCategoryRepository.findById(productModifyReqDto.productCategoryId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY));

        // 썸네일 이미지 수정
        updateThumbnail(product.getThumbnailFile(), thumbnailFile, product, productModifyReqDto.thumbnailFileId());

        product.modify(productModifyReqDto, productCategory);
        productRepository.save(product);

        // Elastic Search에 저장된 상품명 수정
        updateProductNameToElasticsearch(productName, productModifyReqDto.name());
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

        // Elastic Search에 저장된 상품명 삭제
        elasticProductNameRepository.deleteByName(product.getName());
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

    /**
     * 상품명을 Elastic Search에 저장
     */
    private void insertProductNameToElasticSearch(String name) {
        if (!elasticProductNameRepository.existsByName(name)) {
            elasticProductNameRepository.save(ProductNameDocument.builder()
                .name(name)
                .build());
        }
    }

    /**
     * Elastic Search에 저장된 상품명 수정
     */
    private void updateProductNameToElasticsearch(final String savedProductName, final String productName) {
        Optional<ProductNameDocument> savedProductNameDocument = elasticProductNameRepository.findByName(savedProductName);

        if (savedProductNameDocument.isPresent() && !savedProductName.equals(productName)) {
            // 저장된 상품명이 있거나 상품명이 변경된 경우
            ProductNameDocument productNameDocument = savedProductNameDocument.get();
            productNameDocument.modifyName(productName);
            elasticProductNameRepository.updateProductNameById(productNameDocument);
        } else if (savedProductNameDocument.isEmpty()) {
            // 저장된 상품명이 없는 경우
            elasticProductNameRepository.save(ProductNameDocument.builder()
                .name(productName)
                .build());
        }
    }
}
