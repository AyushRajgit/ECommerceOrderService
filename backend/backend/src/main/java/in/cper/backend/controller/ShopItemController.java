package in.cper.backend.controller;

import in.cper.backend.dto.ShoppingCartDTO;
import in.cper.backend.entity.CartItem;
import in.cper.backend.service.ShopItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/amazon/shopping")
public class ShopItemController {

    private ShopItemService shopItemService;

    @Autowired
    public ShopItemController(ShopItemService shopItemService) {
        this.shopItemService = shopItemService;
    }

    @PostMapping("/addToCart")
    @PreAuthorize("hasAnyRole('USER','MANAGER')")
    public ResponseEntity<ShoppingCartDTO> addToCart(@RequestParam int customerId, @RequestParam int productId, @RequestParam int quantity, @AuthenticationPrincipal UserDetails currentUser) {
        Set<CartItem> cartItem = shopItemService.addToCart(customerId, productId, quantity, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ShoppingCartDTO(
                cartItem,
                "Successfully added to your cart! Enjoy your shopping"
        ));
    }

    @GetMapping("/viewMyCart")
    @PreAuthorize("hasAnyRole('USER','MANAGER')")
    public ResponseEntity<ShoppingCartDTO> viewMyCart(@RequestParam int customerId, @AuthenticationPrincipal UserDetails currentUser) {
        Set<CartItem> myCartItems = shopItemService.getCartItems(customerId, currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(new ShoppingCartDTO(
                myCartItems,
                "Here, is your shopping cart! checkout so it could reach at your door asap"
        ));
    }

    @DeleteMapping("/removeFromCart")
    @PreAuthorize("hasAnyRole('USER','MANAGER')")
    public ResponseEntity<ShoppingCartDTO> removeFromCart(@RequestParam int customerId, @RequestParam int productId, @AuthenticationPrincipal UserDetails currentUser) {
        Set<CartItem> freshCartItem = shopItemService.removeFromCart(customerId, productId, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ShoppingCartDTO(
                freshCartItem,
                "Successfully removed product from your cart! Enjoy your shopping"
        ));
    }
}
