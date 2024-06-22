package potatowoong.potatomallback.domain.product.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import potatowoong.potatomallback.domain.product.dto.response.ProductNameResDto;
import potatowoong.potatomallback.domain.product.repository.ElasticProductNameRepository;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticProductNameRepository elasticProductNameRepository;

    public List<ProductNameResDto> searchProductNameWithAutoComplete(String name) {
        return elasticProductNameRepository.searchProductName(name).stream()
            .map(ProductNameResDto::of)
            .toList();
    }
}
