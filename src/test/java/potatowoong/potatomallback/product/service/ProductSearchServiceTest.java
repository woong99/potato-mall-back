package potatowoong.potatomallback.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import potatowoong.potatomallback.domain.product.document.ProductNameDocument;
import potatowoong.potatomallback.domain.product.dto.response.ProductNameResDto;
import potatowoong.potatomallback.domain.product.repository.ElasticProductNameRepository;
import potatowoong.potatomallback.domain.product.service.ProductSearchService;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {

    @Mock
    private ElasticProductNameRepository elasticProductNameRepository;

    @InjectMocks
    private ProductSearchService productSearchService;

    @Nested
    @DisplayName("상품명 검색(자동완성)")
    class 상품명_검색_자동완성 {

        @DisplayName("성공")
        @Test
        void 성공() {
            // given
            String name = "감";

            given(elasticProductNameRepository.searchProductName(name)).willReturn(List.of(ProductNameDocument.builder()
                .name("감자")
                .build()));

            // when
            List<ProductNameResDto> result = productSearchService.searchProductNameWithAutoComplete(name);

            // then
            assertThat(result).isNotEmpty();

            then(elasticProductNameRepository).should().searchProductName(name);
        }
    }
}