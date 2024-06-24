package potatowoong.potatomallback.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto.Search;
import potatowoong.potatomallback.domain.product.repository.ProductRepository;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

@Service
@RequiredArgsConstructor
public class UserProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<Search> getUserProductList(PageRequestDto pageRequestDto) {
        return productRepository.findUserProductWithPage(pageRequestDto);
    }
}
