package potatowoong.potatomallback.domain.product.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.product.entity.ProductLike;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    boolean existsByProductProductIdAndMemberUserId(long productId, String userId);

    Optional<ProductLike> findByProductProductIdAndMemberUserId(long productId, String userId);

    int countByProductProductIdAndMemberUserId(long productId, String userId);
}
