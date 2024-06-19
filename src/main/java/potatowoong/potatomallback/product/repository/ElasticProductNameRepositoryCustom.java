package potatowoong.potatomallback.product.repository;

import java.util.List;
import potatowoong.potatomallback.product.document.ProductNameDocument;

public interface ElasticProductNameRepositoryCustom {

    void updateProductNameById(ProductNameDocument productNameDocument);

    List<ProductNameDocument> searchProductName(String name);
}
