package in.cper.backend.repository;

import in.cper.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisterCustomerRepository extends JpaRepository<Customer,Integer> {
    Optional<Customer> findFirstByEmailOrderByCustomerIdAsc(String email);
}
