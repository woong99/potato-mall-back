package potatowoong.potatomallback.domain.product.controller;

import static potatowoong.potatomallback.global.common.LogMessage.ADD;
import static potatowoong.potatomallback.global.common.LogMessage.MODIFY;
import static potatowoong.potatomallback.global.common.LogMessage.PRODUCT_CATEGORY_MANAGEMENT;
import static potatowoong.potatomallback.global.common.LogMessage.REMOVE;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_DETAIL;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_LIST;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.auth.service.AdminLogService;
import potatowoong.potatomallback.domain.product.dto.request.ProductCategoryReqDto.ProductCategoryAddReqDto;
import potatowoong.potatomallback.domain.product.dto.request.ProductCategoryReqDto.ProductCategoryModifyReqDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategoryDetailResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategoryListResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductCategoryResDto.ProductCategorySearchResDto;
import potatowoong.potatomallback.domain.product.service.ProductCategoryService;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.common.ResponseText;

@RestController
@RequestMapping("/api/admin/product-category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    private final AdminLogService adminLogService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponseEntity<PageResponseDto<ProductCategorySearchResDto>>> searchProductCategory(PageRequestDto pageRequestDto) {
        // 상품 카테고리 목록 조회
        PageResponseDto<ProductCategorySearchResDto> result = productCategoryService.getProductCategoryList(pageRequestDto);

        // 로그 저장
        adminLogService.addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, SEARCH_LIST);

        return ResponseEntity.ok(ApiResponseEntity.of(result));
    }

    /**
     * 상품 카테고리 상세 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseEntity<ProductCategoryDetailResDto>> getProductCategory(@PathVariable Long id) {
        // 상품 카테고리 상세 조회
        ProductCategoryDetailResDto productCategory = productCategoryService.getProductCategory(id);

        // 로그 저장
        adminLogService.addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, SEARCH_DETAIL, id, productCategory.name());

        return ResponseEntity.ok(ApiResponseEntity.of(productCategory));
    }

    /**
     * 상품 카테고리 전체 조회 API
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponseEntity<List<ProductCategoryListResDto>>> getAllProductCategoryList() {
        return ResponseEntity.ok(ApiResponseEntity.of(productCategoryService.getAllProductCategoryList()));
    }

    /**
     * 상품 카테고리 등록 API
     */
    @PostMapping
    public ResponseEntity<ApiResponseEntity<String>> addProductCategory(@Valid @RequestBody ProductCategoryAddReqDto dto) {
        // 상품 카테고리 등록
        productCategoryService.addProductCategory(dto);

        // 로그 저장
        adminLogService.addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, ADD, "", dto.name());

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_ADD_PRODUCT_CATEGORY));
    }

    /**
     * 상품 카테고리 수정 API
     */
    @PutMapping
    public ResponseEntity<ApiResponseEntity<String>> modifyProductCategory(@Valid @RequestBody ProductCategoryModifyReqDto dto) {
        // 상품 카테고리 수정
        productCategoryService.modifyProductCategory(dto);

        // 로그 저장
        adminLogService.addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, MODIFY, dto.productCategoryId(), dto.name());

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_MODIFY_PRODUCT_CATEGORY));
    }

    /**
     * 상품 카테고리 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseEntity<String>> removeProductCategory(@PathVariable Long id) {
        // 로그 저장을 위한 조회
        final String categoryName = productCategoryService.getProductCategoryName(id);

        // 상품 카테고리 삭제
        productCategoryService.removeProductCategory(id);

        // 로그 저장
        adminLogService.addAdminLog(PRODUCT_CATEGORY_MANAGEMENT, REMOVE, id, categoryName);

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_REMOVE_PRODUCT_CATEGORY));
    }
}
