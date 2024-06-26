package potatowoong.potatomallback.domain.product.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.product.dto.request.ProductCategoryReqDto.ProductCategoryAddReqDto;
import potatowoong.potatomallback.domain.product.dto.request.ProductCategoryReqDto.ProductCategoryModifyReqDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategoryDetailResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategoryListResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategorySearchResDto;
import potatowoong.potatomallback.domain.product.repository.ProductCategoryRepository;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.domain.product.entity.ProductCategory;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public PageResponseDto<ProductCategorySearchResDto> getProductCategoryList(PageRequestDto pageRequestDto) {
        return productCategoryRepository.findProductCategoryWithPage(pageRequestDto);
    }

    @Transactional(readOnly = true)
    public ProductCategoryDetailResDto getProductCategory(final long id) {
        return productCategoryRepository.findWithProductsByProductCategoryId(id)
            .map(ProductCategoryDetailResDto::of)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY));
    }

    @Transactional(readOnly = true)
    public String getProductCategoryName(final long id) {
        return productCategoryRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY))
            .getName();
    }
    
    @Transactional(readOnly = true)
    public List<ProductCategoryListResDto> getAllProductCategoryList() {
        return productCategoryRepository.findAll().stream()
            .map(ProductCategoryListResDto::of)
            .toList();
    }

    @Transactional
    public void addProductCategory(ProductCategoryAddReqDto dto) {
        // 카테고리명 중복 검사
        Optional<ProductCategory> savedProductCategory = productCategoryRepository.findByName(dto.name());
        if (savedProductCategory.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_CATEGORY_NAME);
        }

        productCategoryRepository.save(ProductCategory.addOf(dto));
    }

    @Transactional
    public void modifyProductCategory(ProductCategoryModifyReqDto dto) {
        // 카테고리 조회
        ProductCategory productCategory = productCategoryRepository.findById(dto.productCategoryId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY));

        // 카테고리명 중복 검사
        Optional<ProductCategory> duplicateProductCategory = productCategoryRepository.findByName(dto.name());
        if (duplicateProductCategory.isPresent() && !duplicateProductCategory.get().getProductCategoryId().equals(dto.productCategoryId())) {
            throw new CustomException(ErrorCode.DUPLICATED_CATEGORY_NAME);
        }

        productCategory.modify(dto.name());
        productCategoryRepository.save(productCategory);
    }

    @Transactional
    public void removeProductCategory(final long productCategoryId) {
        // 카테고리 조회
        ProductCategory savedProductCategory = productCategoryRepository.findById(productCategoryId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY));

        // 카테고리에 상품이 존재하는지 확인
        if (productRepository.existsByProductCategory(savedProductCategory)) {
            throw new CustomException(ErrorCode.EXIST_PRODUCT_IN_CATEGORY);
        }
        productCategoryRepository.delete(savedProductCategory);
    }
}
