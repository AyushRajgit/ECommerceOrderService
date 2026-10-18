package in.cper.backend.dto;


import in.cper.backend.entity.CartItem;

import java.util.List;
import java.util.Set;

public class ShoppingCartDTO {
    private Set<CartItem> cartItems;
    private String message;

    public ShoppingCartDTO(Set<CartItem> cartItems, String message) {
        this.cartItems = cartItems;
        this.message = message;
    }

    public Set<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Set<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}