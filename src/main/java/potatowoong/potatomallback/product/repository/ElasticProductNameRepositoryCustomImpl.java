package potatowoong.potatomallback.product.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Repository;
import potatowoong.potatomallback.product.document.ProductNameDocument;

@Repository
@RequiredArgsConstructor
public class ElasticProductNameRepositoryCustomImpl implements ElasticProductNameRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void updateProductNameById(ProductNameDocument productNameDocument) {
        Document document = elasticsearchOperations.getElasticsearchConverter().mapObject(productNameDocument);

        UpdateQuery updateQuery = UpdateQuery.builder(productNameDocument.getId())
            .withDocument(document)
            .build();
        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("product_name"));
    }
}
