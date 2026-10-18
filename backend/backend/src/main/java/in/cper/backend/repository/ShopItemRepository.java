package in.cper.backend.repository;

import in.cper.backend.entity.ShoppingCart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopItemRepository extends JpaRepository<ShoppingCart, Integer> {
    @EntityGraph(attributePaths = {"customer", "cartItems"})
    Optional<ShoppingCart> findByCustomer_CustomerId(int customerId);
}
