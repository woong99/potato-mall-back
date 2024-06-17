package potatowoong.potatomallback.product.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.product.entity.Product;
import potatowoong.potatomallback.product.entity.ProductCategory;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @EntityGraph(attributePaths = "thumbnailFile")
    Optional<Product> findWithThumbnailFileByProductId(Long productId);

    boolean existsByProductCategory(ProductCategory productCategory);
    
    Optional<Product> findByName(String name);
}
