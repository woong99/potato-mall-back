package potatowoong.potatomallback.domain.cart.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.cart.entity.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @EntityGraph(attributePaths = {"product", "product.thumbnailFile"})
    List<ShoppingCart> findAllByMemberUserId(String userId);

    Optional<ShoppingCart> findByShoppingCartIdAndMemberUserId(long shoppingCartId, String userId);

    int countByMemberUserId(String userId);
}
