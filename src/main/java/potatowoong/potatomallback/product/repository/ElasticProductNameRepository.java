package potatowoong.potatomallback.product.repository;

import java.util.Optional;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import potatowoong.potatomallback.product.document.ProductNameDocument;

public interface ElasticProductNameRepository extends ElasticsearchRepository<ProductNameDocument, String>, ElasticProductNameRepositoryCustom {

    boolean existsByName(String name);

    Optional<ProductNameDocument> findByName(String name);

    void deleteByName(String name);
}
