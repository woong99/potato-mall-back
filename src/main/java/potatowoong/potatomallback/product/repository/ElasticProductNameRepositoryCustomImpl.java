package potatowoong.potatomallback.product.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
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

    @Override
    public List<ProductNameDocument> searchProductName(String name) {
        // 조회 쿼리 생성
        NativeQuery query = NativeQuery.builder()
            .withQuery(q ->
                q.matchPhrasePrefix(v ->
                    v.field("name")
                        .query(name)
                )
            )
            .withFields("name")
            .build();

        SearchHits<ProductNameDocument> searchHits = elasticsearchOperations.search(query, ProductNameDocument.class);

        return searchHits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();
    }
}
