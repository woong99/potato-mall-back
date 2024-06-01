package potatowoong.potatomallback.product.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.product.entity.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, ProductCategoryRepositoryCustom {

    Optional<ProductCategory> findByName(String categoryName);
}
