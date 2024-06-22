package potatowoong.potatomallback.domain.product.repository;

import java.util.List;
import potatowoong.potatomallback.domain.product.document.ProductNameDocument;

public interface ElasticProductNameRepositoryCustom {

    void updateProductNameById(ProductNameDocument productNameDocument);

    List<ProductNameDocument> searchProductName(String name);
}
