package in.cper.backend.repository;

import in.cper.backend.entity.Orders;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckOutRepository extends JpaRepository<Orders, Integer> {
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "payment"})
    List<Orders> findByCustomer_CustomerIdOrderByDateDescTimeDesc(int customerId);
}
