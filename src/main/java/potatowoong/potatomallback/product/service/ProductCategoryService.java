package potatowoong.potatomallback.product.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.product.dto.request.ProductCategoryAddReqDto;
import potatowoong.potatomallback.product.dto.request.ProductCategoryModifyReqDto;
import potatowoong.potatomallback.product.dto.response.ProductCategoryDetailResDto;
import potatowoong.potatomallback.product.dto.response.ProductCategorySearchResDto;
import potatowoong.potatomallback.product.entity.ProductCategory;
import potatowoong.potatomallback.product.repository.ProductCategoryRepository;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    @Transactional(readOnly = true)
    public PageResponseDto<ProductCategorySearchResDto> getProductCategoryList(PageRequestDto pageRequestDto) {
        return productCategoryRepository.findProductCategoryWithPage(pageRequestDto);
    }

    @Transactional(readOnly = true)
    public ProductCategoryDetailResDto getProductCategory(final long id) {
        // TODO : 연관된 상품 목록 추가

        return productCategoryRepository.findById(id)
            .map(ProductCategoryDetailResDto::of)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY));
    }

    @Transactional(readOnly = true)
    public String getProductCategoryName(final long id) {
        return productCategoryRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY))
            .getName();
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

        // TODO : 상품 카테고리에 상품이 존재하는지 확인

        savedProductCategory.deleteEntity();
        productCategoryRepository.save(savedProductCategory);
    }
}
