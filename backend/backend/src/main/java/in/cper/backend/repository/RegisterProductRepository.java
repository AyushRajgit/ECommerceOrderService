package in.cper.backend.repository;

import in.cper.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisterProductRepository extends JpaRepository<Product, Integer> {

}
