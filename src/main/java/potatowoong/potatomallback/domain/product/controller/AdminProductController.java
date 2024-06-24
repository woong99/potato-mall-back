package potatowoong.potatomallback.domain.product.controller;

import static potatowoong.potatomallback.global.common.LogMessage.ADD;
import static potatowoong.potatomallback.global.common.LogMessage.MODIFY;
import static potatowoong.potatomallback.global.common.LogMessage.PRODUCT_MANAGEMENT;
import static potatowoong.potatomallback.global.common.LogMessage.REMOVE;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_DETAIL;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_LIST;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import potatowoong.potatomallback.domain.auth.service.AdminLogService;
import potatowoong.potatomallback.domain.product.dto.request.ProductReqDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductDetailResDto;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductSearchResDto;
import potatowoong.potatomallback.domain.product.service.AdminProductService;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.common.ResponseText;

@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    private final AdminLogService adminLogService;

    /**
     * 상품 목록 조회 API
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseEntity<PageResponseDto<ProductSearchResDto>>> getProductList(PageRequestDto pageRequestDto) {
        // 상품 목록 조회
        PageResponseDto<ProductSearchResDto> productSearchResDto = adminProductService.getProductList(pageRequestDto);

        // 관리자 로그 등록
        adminLogService.addAdminLog(PRODUCT_MANAGEMENT, SEARCH_LIST);

        return ResponseEntity.ok(ApiResponseEntity.of(productSearchResDto));
    }

    /**
     * 상품 상세 조회 API
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponseEntity<ProductDetailResDto>> getProduct(@PathVariable Long productId) {
        // 상품 조회
        ProductDetailResDto productDetailResDto = adminProductService.getProduct(productId);

        // 관리자 로그 등록
        adminLogService.addAdminLog(PRODUCT_MANAGEMENT, SEARCH_DETAIL, productId, productDetailResDto.name());

        return ResponseEntity.ok(ApiResponseEntity.of(productDetailResDto));
    }

    /**
     * 상품 등록 API
     */
    @PostMapping
    public ResponseEntity<ApiResponseEntity<String>> addProduct(@Valid @RequestPart ProductReqDto.ProductAddReqDto productAddReqDto, @RequestPart(required = false) MultipartFile thumbnailFile) {
        // 상품 등록
        adminProductService.addProduct(productAddReqDto, thumbnailFile);

        // 관리자 로그 등록
        adminLogService.addAdminLog(PRODUCT_MANAGEMENT, ADD, "", productAddReqDto.name());

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_ADD_PRODUCT));
    }

    /**
     * 상품 수정 API
     */
    @PutMapping
    public ResponseEntity<ApiResponseEntity<String>> modifyProduct(@Valid @RequestPart ProductReqDto.ProductModifyReqDto productModifyReqDto, @RequestPart(required = false) MultipartFile thumbnailFile) {
        // 상품 수정
        adminProductService.modifyProduct(productModifyReqDto, thumbnailFile);

        // 관리자 로그 등록
        adminLogService.addAdminLog(PRODUCT_MANAGEMENT, MODIFY, productModifyReqDto.productId(), productModifyReqDto.name());

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_MODIFY_PRODUCT));
    }

    /**
     * 상품 삭제 API
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponseEntity<String>> removeProduct(@PathVariable Long productId) {
        // 상품 삭제
        adminProductService.removeProduct(productId);

        // 관리자 로그 등록
        adminLogService.addAdminLog(PRODUCT_MANAGEMENT, REMOVE, productId, "");

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_REMOVE_PRODUCT));
    }
}
