package in.cper.backend.dto;

import in.cper.backend.entity.Customer;
import in.cper.backend.entity.OrderItems;
import in.cper.backend.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class CheckOutDTO {
    private int orderId;
    private String transactionId;
    private LocalDate date;
    private LocalTime time;
    private Customer customer;
    private Set<OrderItems> orderItems;
    private String paymentMethod;
    private String paidAt;
    private Status status;
    private BigDecimal totalAmount;

    public CheckOutDTO(int orderId, String transactionId, LocalDate date, LocalTime time, Customer customer, Set<OrderItems> orderItems, String paymentMethod, String paidAt, Status status, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.date = date;
        this.time = time;
        this.customer = customer;
        this.orderItems = orderItems;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<OrderItems> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Set<OrderItems> orderItems) {
        this.orderItems = orderItems;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}

