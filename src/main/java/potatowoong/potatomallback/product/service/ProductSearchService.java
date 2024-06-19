package potatowoong.potatomallback.product.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import potatowoong.potatomallback.product.dto.response.ProductNameResDto;
import potatowoong.potatomallback.product.repository.ElasticProductNameRepository;

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
