package potatowoong.potatomallback.domain.cart.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import potatowoong.potatomallback.domain.cart.entity.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @EntityGraph(attributePaths = {"product", "product.thumbnailFile"})
    List<ShoppingCart> findAllByMemberUserId(String userId);

    Optional<ShoppingCart> findByShoppingCartIdAndMemberUserId(long shoppingCartId, String userId);

    int countByMemberUserId(String userId);

    List<ShoppingCart> findByShoppingCartIdInAndMemberUserId(List<Long> shoppingCartIds, String userId);

    @Modifying
    @Query("DELETE FROM ShoppingCart s WHERE s.shoppingCartId IN :shoppingCartIds")
    void deleteByShoppingCartIdIn(List<Long> shoppingCartIds);
}
