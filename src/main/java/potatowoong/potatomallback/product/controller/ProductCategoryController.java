package potatowoong.potatomallback.product.controller;

import jakarta.validation.Valid;
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
import potatowoong.potatomallback.auth.service.AdminLogService;
import potatowoong.potatomallback.common.ApiResponseEntity;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.product.dto.request.ProductCategoryAddReqDto;
import potatowoong.potatomallback.product.dto.request.ProductCategoryModifyReqDto;
import potatowoong.potatomallback.product.dto.response.ProductCategoryDetailResDto;
import potatowoong.potatomallback.product.dto.response.ProductCategorySearchResDto;
import potatowoong.potatomallback.product.service.ProductCategoryService;

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
        adminLogService.addAdminLog("상품 카테고리 관리", "목록 조회");

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
        adminLogService.addAdminLog("상품 카테고리 관리", "상세 조회", id, productCategory.name());

        return ResponseEntity.ok(ApiResponseEntity.of(productCategory));
    }

    /**
     * 상품 카테고리 등록 API
     */
    @PostMapping
    public ResponseEntity<ApiResponseEntity<String>> addProductCategory(@Valid @RequestBody ProductCategoryAddReqDto dto) {
        // 상품 카테고리 등록
        productCategoryService.addProductCategory(dto);

        // 로그 저장
        adminLogService.addAdminLog("상품 카테고리 관리", "등록", "", dto.name());

        return ResponseEntity.ok(ApiResponseEntity.of("상품 카테고리 등록 성공"));
    }

    /**
     * 상품 카테고리 수정 API
     */
    @PutMapping
    public ResponseEntity<ApiResponseEntity<String>> modifyProductCategory(@Valid @RequestBody ProductCategoryModifyReqDto dto) {
        // 상품 카테고리 수정
        productCategoryService.modifyProductCategory(dto);

        // 로그 저장
        adminLogService.addAdminLog("상품 카테고리 관리", "수정", dto.productCategoryId(), dto.name());

        return ResponseEntity.ok(ApiResponseEntity.of("상품 카테고리 수정 성공"));
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
        adminLogService.addAdminLog("상품 카테고리 관리", "삭제", id, categoryName);

        return ResponseEntity.ok(ApiResponseEntity.of("상품 카테고리 삭제 성공"));
    }
}
