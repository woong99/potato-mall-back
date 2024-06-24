package potatowoong.potatomallback.domain.product.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.product.dto.response.ProductNameResDto;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto;
import potatowoong.potatomallback.domain.product.service.ProductSearchService;
import potatowoong.potatomallback.domain.product.service.ProductService;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class UserProductController {

    private final ProductService productService;

    private final ProductSearchService productSearchService;

    /**
     * 상품 목록 조회 API
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseEntity<PageResponseDto<UserProductResDto.Search>>> search(PageRequestDto pageRequestDto) {
        // 상품 목록 조회
        PageResponseDto<UserProductResDto.Search> productSearchResDto = productService.getUserProductList(pageRequestDto);

        return ResponseEntity.ok(ApiResponseEntity.of(productSearchResDto));
    }

    /**
     * 상품명 검색(자동완성) API
     */
    @GetMapping("/search-with-auto-complete")
    public ResponseEntity<ApiResponseEntity<List<ProductNameResDto>>> searchWithAutoComplete(@RequestParam String searchWord) {
        // 상품명 조회
        List<ProductNameResDto> result = productSearchService.searchProductNameWithAutoComplete(searchWord);

        return ResponseEntity.ok(ApiResponseEntity.of(result));
    }
}
