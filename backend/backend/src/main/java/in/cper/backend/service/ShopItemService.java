package in.cper.backend.service;

import in.cper.backend.entity.CartItem;
import in.cper.backend.entity.Customer;
import in.cper.backend.entity.Product;
import in.cper.backend.entity.ShoppingCart;
import in.cper.backend.exceptions.NoStockAvailableException;
import in.cper.backend.exceptions.NotFoundException;
import in.cper.backend.repository.CartItemRepository;
import in.cper.backend.repository.RegisterCustomerRepository;
import in.cper.backend.repository.RegisterProductRepository;
import in.cper.backend.repository.ShopItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class ShopItemService {

    private ShopItemRepository shopItemRepository;
    private RegisterProductRepository registerProductRepository;
    private RegisterCustomerRepository registerCustomerRepository;
    private CartItemRepository cartItemRepository;

    @Autowired
    public ShopItemService(ShopItemRepository shopItemRepository, RegisterProductRepository registerProductRepository, RegisterCustomerRepository registerCustomerRepository, CartItemRepository cartItemRepository) {
        this.shopItemRepository = shopItemRepository;
        this.registerProductRepository = registerProductRepository;
        this.registerCustomerRepository = registerCustomerRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public Set<CartItem> addToCart(int customerId, int productId, int quantity, UserDetails currentUser) {
        Customer customer = requireCustomerAccess(customerId, currentUser);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = registerProductRepository.findById(productId).orElseThrow(
                () -> new NotFoundException("Product with id :" + productId + "not found while adding into cart")
        );

        ShoppingCart shoppingCart = shopItemRepository.findByCustomer_CustomerId(customerId).orElseGet(() -> {
            ShoppingCart newCart = new ShoppingCart();
            newCart.setCustomer(customer);
            return newCart;
        });

        CartItem newCartItem = shoppingCart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getProductId() == productId)
                .findFirst()
                .orElse(new CartItem());

        newCartItem.setProduct(product);
        long requestedTotal = (long) newCartItem.getQuantity() + quantity;
        if (requestedTotal > product.getAvailableQuantity()) {
            throw new NoStockAvailableException("Demanded quantity for product is more than available stock");
        }
        newCartItem.setQuantity((int) requestedTotal);
        newCartItem.setPriceAtCheckout(product.getProductPrice()
                .multiply(BigDecimal.valueOf(newCartItem.getQuantity())));
        newCartItem.setShoppingCart(shoppingCart);

        shoppingCart.getCartItems().add(newCartItem);
        shoppingCart.setCustomer(customer);

        // used Cascase.ALL so saving parent entity would automatically save related one's.
        // cartItemRepository.save(newCartItem);
        shopItemRepository.save(shoppingCart);

        return shoppingCart.getCartItems();
    }

    public Set<CartItem> getCartItems(int customerId, UserDetails currentUser) {
        requireCustomerAccess(customerId, currentUser);
        ShoppingCart shoppingCart = shopItemRepository.findByCustomer_CustomerId(customerId).orElseThrow(
                () -> new NotFoundException("No cart found for the customer with id : " + customerId)
        );

        Set<CartItem> cartItems = shoppingCart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new NotFoundException("Shopping cart with no cart items found for the customer with id : " + customerId);
        }

        return cartItems;
    }

    @Transactional
    public Set<CartItem> removeFromCart(int customerId, int productId, UserDetails currentUser) {
        requireCustomerAccess(customerId, currentUser);
        ShoppingCart shoppingCart = shopItemRepository.findByCustomer_CustomerId(customerId).orElseThrow(
                () -> new NotFoundException("No shopping cart found")
        );

        int size = shoppingCart.getCartItems().size();
        CartItem targetRef = null;
        for (CartItem item : shoppingCart.getCartItems()) {
            if (item.getProduct().getProductId() == productId) {
                targetRef = item;
                break;
            }
        }

        if (targetRef != null) {
            if (shoppingCart.getCartItems().contains(targetRef)) shoppingCart.getCartItems().remove(targetRef);
            shoppingCart.getCartItems().remove(targetRef);
        } else {
            throw new NotFoundException("Product with id :" + productId + " for customer with id :" + " not found while removing from cart");
        }

        shopItemRepository.save(shoppingCart);
        return shoppingCart.getCartItems();
    }

    private Customer requireCustomerAccess(int customerId, UserDetails currentUser) {
        Customer customer = registerCustomerRepository.findById(customerId).orElseThrow(
                () -> new NotFoundException("Customer not found")
        );

        boolean isManager = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MANAGER"::equals);
        if (!isManager && (customer.getEmail() == null || !customer.getEmail().equalsIgnoreCase(currentUser.getUsername()))) {
            throw new NotFoundException("Customer not found");
        }

        return customer;
    }

}
