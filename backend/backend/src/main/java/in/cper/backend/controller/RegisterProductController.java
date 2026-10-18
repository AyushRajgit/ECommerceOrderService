package in.cper.backend.controller;

import in.cper.backend.dto.ProductPageDTO;
import in.cper.backend.entity.Product;
import in.cper.backend.service.RegisterProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amazon/register-product")
public class RegisterProductController {

    private RegisterProductService registerProductService;

    @Autowired
    public RegisterProductController(RegisterProductService registerProductService) {
        this.registerProductService = registerProductService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Product> registerProduct(@RequestBody Product product){
        registerProductService.register(product);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/addAll")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Product> registerAllProduct(@RequestBody List<Product> products){
        registerProductService.registerAll(products);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<ProductPageDTO> getAllProducts(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        ProductPageDTO products = registerProductService.getAll(pageNo, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable int productId){
        Product product = registerProductService.get(productId);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @PatchMapping("/update")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Product> updateProduct(@RequestBody Product product){
        Product updatedProduct = registerProductService.update(product);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> deleteProductById(@RequestParam int productId){
        registerProductService.remove(productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Product with id " + productId + " has been deleted");
    }
}
