package in.cper.backend.service;

import in.cper.backend.dto.ProductPageDTO;
import in.cper.backend.entity.Product;
import in.cper.backend.exceptions.EmptyRequestException;
import in.cper.backend.exceptions.NotFoundException;
import in.cper.backend.exceptions.UnableToSaveException;
import in.cper.backend.repository.RegisterProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegisterProductService {

    private RegisterProductRepository registerProductRepository;

    @Autowired
    public RegisterProductService(RegisterProductRepository registerProductRepository) {
        this.registerProductRepository = registerProductRepository;
    }

    @Transactional
    public void register(Product product) {
        Product savedProduct = registerProductRepository.save(product);
        if (savedProduct == null) {
            throw new UnableToSaveException("Product unable to save in database");
        }
    }

    @Transactional
    public void registerAll(List<Product> products) {
        if (products == null || products.isEmpty()) {
            throw new EmptyRequestException("Empty product list while registering");
        }

        for (Product product : products) {
            register(product);
        }
    }

    public Product get(int productId) {
        Product product = registerProductRepository.findById(productId).orElseThrow(
                () -> new NotFoundException("Product not found with id " + productId)
        );

        return product;
    }

    public ProductPageDTO getAll(int pageNo, int pageSize) {
        if (pageNo < 0) {
            throw new IllegalArgumentException("pageNo must be zero or greater");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }

        Page<Product> productPage = registerProductRepository.findAll(
                PageRequest.of(pageNo, pageSize, Sort.by("productId").ascending())
        );

        return new ProductPageDTO(
                productPage.getContent(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }

    @Transactional
    public Product update(Product product) {
        Product savedProduct = registerProductRepository.findById(product.getProductId()).orElseThrow(
                () -> new NotFoundException("Product not found with id " + product.getProductId() + " while updating")
        );

        savedProduct.setAvailableQuantity(product.getAvailableQuantity());
        savedProduct.setMerchantDetail(product.getMerchantDetail());
        savedProduct.setProductName(product.getProductName());
        savedProduct.setProductPrice(product.getProductPrice());
        savedProduct.setProductDescription(product.getProductDescription());

        Product savingNewProduct = registerProductRepository.save(savedProduct);
        if (savingNewProduct == null) {
            throw new UnableToSaveException("Product unable to save in database after updating");
        }

        return savingNewProduct;
    }

    @Transactional
    public void remove(int productId) {
        Product savedProduct = registerProductRepository.findById(productId).orElseThrow(
                () -> new NotFoundException("Product not found with id " + productId + " while deleting")
        );

        registerProductRepository.delete(savedProduct);
    }
}
