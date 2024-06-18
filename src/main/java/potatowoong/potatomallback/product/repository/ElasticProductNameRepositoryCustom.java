package potatowoong.potatomallback.product.repository;

import potatowoong.potatomallback.product.document.ProductNameDocument;

public interface ElasticProductNameRepositoryCustom {

    void updateProductNameById(ProductNameDocument productNameDocument);
}
