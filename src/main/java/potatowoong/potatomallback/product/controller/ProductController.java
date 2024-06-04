package potatowoong.potatomallback.product.controller;

import static potatowoong.potatomallback.common.LogMessage.ADD;
import static potatowoong.potatomallback.common.LogMessage.MODIFY;
import static potatowoong.potatomallback.common.LogMessage.PRODUCT_MANAGEMENT;
import static potatowoong.potatomallback.common.LogMessage.REMOVE;
import static potatowoong.potatomallback.common.LogMessage.SEARCH_DETAIL;
import static potatowoong.potatomallback.common.LogMessage.SEARCH_LIST;

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
import potatowoong.potatomallback.auth.service.AdminLogService;
import potatowoong.potatomallback.common.ApiResponseEntity;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.product.dto.request.ProductReqDto;
import potatowoong.potatomallback.product.dto.request.ProductReqDto.ProductAddReqDto;
import potatowoong.potatomallback.product.dto.response.ProductResDto.ProductDetailResDto;
import potatowoong.potatomallback.product.dto.response.ProductResDto.ProductSearchResDto;
import potatowoong.potatomallback.product.service.ProductService;

@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final AdminLogService adminLogService;

    /**
     * 상품 목록 조회 API
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseEntity<PageResponseDto<ProductSearchResDto>>> getProductList(PageRequestDto pageRequestDto) {
        // 상품 목록 조회
        PageResponseDto<ProductSearchResDto> productSearchResDto = productService.getProductList(pageRequestDto);

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
        ProductDetailResDto productDetailResDto = productService.getProduct(productId);

        // 관리자 로그 등록
        adminLogService.addAdminLog(PRODUCT_MANAGEMENT, SEARCH_DETAIL, productId, productDetailResDto.name());

        return ResponseEntity.ok(ApiResponseEntity.of(productDetailResDto));
    }

    /**
     * 상품 등록 API
     */
    @PostMapping
    public ResponseEntity<ApiResponseEntity<String>> addProduct(@Valid @RequestPart ProductAddReqDto productAddReqDto, @RequestPart(required = false) MultipartFile thumbnailFile) {
        // 상품 등록
        productService.addProduct(productAddReqDto, thumbnailFile);

        // 관리자 로그 등록
        adminLogService.addAdminLog(PRODUCT_MANAGEMENT, ADD, "", productAddReqDto.name());

        return ResponseEntity.ok(ApiResponseEntity.of("상품 등록 성공"));
    }

    /**
     * 상품 수정 API
     */
    @PutMapping
    public ResponseEntity<ApiResponseEntity<String>> modifyProduct(@Valid @RequestPart ProductReqDto.ProductModifyReqDto productModifyReqDto, @RequestPart(required = false) MultipartFile thumbnailFile) {
        // 상품 수정
        productService.modifyProduct(productModifyReqDto, thumbnailFile);

        // 관리자 로그 등록
        adminLogService.addAdminLog(PRODUCT_MANAGEMENT, MODIFY, productModifyReqDto.productId(), productModifyReqDto.name());

        return ResponseEntity.ok(ApiResponseEntity.of("상품 수정 성공"));
    }

    /**
     * 상품 삭제 API
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponseEntity<String>> removeProduct(@PathVariable Long productId) {
        // 상품 삭제
        productService.removeProduct(productId);

        // 관리자 로그 등록
        adminLogService.addAdminLog(PRODUCT_MANAGEMENT, REMOVE, productId, "");

        return ResponseEntity.ok(ApiResponseEntity.of("상품 삭제 성공"));
    }
}
