package in.cper.backend.controller;

import in.cper.backend.entity.Customer;
import in.cper.backend.dto.CustomerProfileDTO;
import in.cper.backend.service.RegisterCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amazon/register-customer")
public class RegisterCustomerController {

    private RegisterCustomerService registerCustomerService;

    @Autowired
    public RegisterCustomerController(RegisterCustomerService registerCustomerService) {
        this.registerCustomerService = registerCustomerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER')")
    public ResponseEntity<CustomerProfileDTO> registerCustomer(@RequestBody Customer customer, @AuthenticationPrincipal UserDetails currentUser) {
        boolean isManager = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MANAGER"::equals);

        Customer savedCustomer = isManager
                ? registerCustomerService.register(customer)
                : registerCustomerService.saveForUser(customer, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(toProfileDTO(savedCustomer));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','MANAGER')")
    public ResponseEntity<CustomerProfileDTO> getCurrentCustomer(@AuthenticationPrincipal UserDetails currentUser) {
        Customer customer = registerCustomerService.getByEmail(currentUser.getUsername());
        return ResponseEntity.ok(toProfileDTO(customer));
    }

    @PostMapping("/addAll")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Customer>> registerAllCustomers(@RequestBody List<Customer> customers) {
        registerCustomerService.registerAll(customers);
        return ResponseEntity.status(HttpStatus.CREATED).body(customers);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> fetchedCustomers = registerCustomerService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(fetchedCustomers);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Customer> getCustomerById(@PathVariable int customerId) {
        Customer fetchedCustomer = registerCustomerService.get(customerId);
        return ResponseEntity.status(HttpStatus.OK).body(fetchedCustomer);
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> deleteCustomerById(@PathVariable int customerId) {
        registerCustomerService.remove(customerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Customer with id " + customerId + " has been deleted");
    }

    private CustomerProfileDTO toProfileDTO(Customer customer) {
        return new CustomerProfileDTO(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getAddress()
        );
    }
}
