package in.cper.backend.service;

import in.cper.backend.entity.Customer;
import in.cper.backend.exceptions.EmptyRequestException;
import in.cper.backend.exceptions.NotFoundException;
import in.cper.backend.exceptions.UnableToSaveException;
import in.cper.backend.repository.RegisterCustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegisterCustomerService {

    private RegisterCustomerRepository registerCustomerRepository;

    @Autowired
    public RegisterCustomerService(RegisterCustomerRepository registerCustomerRepository) {
        this.registerCustomerRepository = registerCustomerRepository;
    }

    @Transactional
    public Customer register(Customer customer) {
        Customer saved = registerCustomerRepository.save(customer);
        if (saved == null) {
            throw new UnableToSaveException("User unable to save in database");
        }
        return saved;
    }

    @Transactional
    public Customer saveForUser(Customer customer, String email) {
        Customer existing = registerCustomerRepository.findFirstByEmailOrderByCustomerIdAsc(email)
                .orElse(null);

        if (existing == null) {
            customer.setCustomerId(0);
            customer.setEmail(email);
            customer.setShoppingCart(null);
            customer.setOrders(new ArrayList<>());
            return register(customer);
        }

        existing.setFirstName(customer.getFirstName());
        existing.setLastName(customer.getLastName());
        existing.setPhoneNumber(customer.getPhoneNumber());
        existing.setAddress(customer.getAddress());
        return registerCustomerRepository.save(existing);
    }

    public Customer getByEmail(String email) {
        return registerCustomerRepository.findFirstByEmailOrderByCustomerIdAsc(email)
                .orElseThrow(() -> new NotFoundException("Delivery profile not found for the signed-in user"));
    }

    @Transactional
    public void registerAll(List<Customer> customers) {
        if (customers == null || customers.size() == 0) {
            throw new EmptyRequestException("Empty user list while registering");
        }

        for (Customer customer : customers) {
            register(customer);
        }
    }

    public Customer get(int id) {
        Customer customer = registerCustomerRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Unable to fetch user with userId : " + id)
        );

        return customer;
    }

    public List<Customer> getAll() {
        List<Customer> customers = registerCustomerRepository.findAll();
        if (customers == null) {
            throw new NotFoundException("Unable to fetch users");
        }
        return customers;
    }

    @Transactional
    public void remove(int id) {
        Customer customer = registerCustomerRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Unable to fetch user with userId : " + id + " while deleting")
        );

        registerCustomerRepository.delete(customer);
    }
}
