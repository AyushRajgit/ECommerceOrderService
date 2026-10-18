package in.cper.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderItemId;

    @ManyToOne
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_Id")
    @JsonIgnore
    private Orders order;

    private int quantity;

    private BigDecimal priceAtCheckout;

    protected OrderItems() {
    }

    public OrderItems(Product product, Orders order, int quantity, BigDecimal priceAtCheckout) {
        this.product = product;
        this.order = order;
        this.quantity = quantity;
        this.priceAtCheckout = priceAtCheckout;
    }

    public Orders getOrder() {
        return order;
    }

    public void setOrder(Orders order) {
        this.order = order;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtCheckout() {
        return priceAtCheckout;
    }

    public void setPriceAtCheckout(BigDecimal priceAtCheckout) {
        this.priceAtCheckout = priceAtCheckout;
    }
}
