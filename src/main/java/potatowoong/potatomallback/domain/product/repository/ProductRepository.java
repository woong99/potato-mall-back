package potatowoong.potatomallback.domain.product.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.domain.product.entity.ProductCategory;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @EntityGraph(attributePaths = "thumbnailFile")
    Optional<Product> findWithThumbnailFileByProductId(Long productId);

    boolean existsByProductCategory(ProductCategory productCategory);

    Optional<Product> findByName(String name);

    List<Product> findByProductIdIn(List<Long> productIds);
}
