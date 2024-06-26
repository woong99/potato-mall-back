package potatowoong.potatomallback.domain.product.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.product.entity.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, ProductCategoryRepositoryCustom {

    Optional<ProductCategory> findByName(String categoryName);

    @EntityGraph(attributePaths = "products")
    Optional<ProductCategory> findWithProductsByProductCategoryId(Long productCategoryId);
}
