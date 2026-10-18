package in.cper.backend.service;

import in.cper.backend.dto.CheckOutDTO;
import in.cper.backend.dto.OrderHistoryDTO;
import in.cper.backend.dto.OrderItemHistoryDTO;
import in.cper.backend.entity.*;
import in.cper.backend.exceptions.NoStockAvailableException;
import in.cper.backend.exceptions.NotFoundException;
import in.cper.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CheckOutService {

    private CheckOutRepository checkOutRepository;
    private OrderItemRepository orderItemRepository;
    private CartItemRepository cartItemRepository;
    private ShopItemRepository shopItemRepository;
    private RegisterCustomerRepository registerCustomerRepository;
    private SimulatedPaymentService simulatedPaymentService;

    public CheckOutService(CheckOutRepository checkOutRepository, CartItemRepository cartItemRepository, ShopItemRepository shopItemRepository, OrderItemRepository orderItemRepository, RegisterCustomerRepository registerCustomerRepository, SimulatedPaymentService simulatedPaymentService) {
        this.checkOutRepository = checkOutRepository;
        this.cartItemRepository = cartItemRepository;
        this.shopItemRepository = shopItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.registerCustomerRepository = registerCustomerRepository;
        this.simulatedPaymentService = simulatedPaymentService;
    }

    @Transactional
    public CheckOutDTO performCheckOut(int customerId, UserDetails currentUser, String paymentMethod) {
        ShoppingCart shoppingCart = shopItemRepository.findByCustomer_CustomerId(customerId).orElseThrow(
                () -> new NotFoundException("ShoppingCart not found with associated to customerId: " + customerId)
        );

        Customer customer = shoppingCart.getCustomer();
        if (customer == null) {
            throw new NotFoundException("Customer not found with associated to customerId: " + customerId + " while checkout");
        }
        requireCustomerAccess(customer, currentUser);

        Set<CartItem> cartItems = shoppingCart.getCartItems();

        Orders newOrder = stageOrderState(customer, cartItems);
        Payment payment = executePayment(newOrder, paymentMethod);
        CheckOutDTO checkOutDTO = dtoMapper(newOrder, payment);

        // releasing resources
        // used Cascase.ALL so deleting parent entity would automatically delete related one's.
        // cartItemRepository.deleteAll(cartItems);
        customer.setShoppingCart(null);
        shoppingCart.setCustomer(null);
        shopItemRepository.delete(shoppingCart);
        logOrder(checkOutDTO);

        return checkOutDTO;
    }

    @Transactional
    public List<OrderHistoryDTO> getMyOrders(String email) {
        Customer customer = registerCustomerRepository.findFirstByEmailOrderByCustomerIdAsc(email)
                .orElseThrow(() -> new NotFoundException("Customer not found for the signed-in user"));

        return checkOutRepository.findByCustomer_CustomerIdOrderByDateDescTimeDesc(customer.getCustomerId())
                .stream()
                .map(this::toOrderHistoryDTO)
                .toList();
    }

    public Orders stageOrderState(Customer customer, Set<CartItem> cartItems) {
        Orders orders = new Orders();
        orders.setCustomer(customer);
        orders = checkOutRepository.save(orders);

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();
            if (quantity <= 0 || product.getAvailableQuantity() < quantity) {
                throw new NoStockAvailableException("Demanded quantity for product is more than available stock");
            }
            product.setAvailableQuantity(product.getAvailableQuantity() - quantity);

            OrderItems finalOrderItems = new OrderItems(
                    product,
                    orders,
                    quantity,
                    cartItem.getPriceAtCheckout()
            );

            orderItemRepository.save(finalOrderItems);
            orders.getOrderItems().add(finalOrderItems);
        }

        performPaymentMaths(orders);
        return orders;
    }

    public void performPaymentMaths(Orders order) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItems orderItems : order.getOrderItems()) {
            totalAmount = totalAmount.add(orderItems.getPriceAtCheckout());
        }

        order.setTotalAmount(totalAmount);
    }

    private Payment executePayment(Orders newOrder, String paymentMethod) {
        Payment payment = simulatedPaymentService.simulate(newOrder, paymentMethod);
        newOrder.getPayment().add(payment);
        newOrder.setDate(payment.getDate());
        newOrder.setTime(payment.getTime());
        newOrder.setTransactionId(payment.getTransactionId());

        checkOutRepository.save(newOrder);
        return payment;
    }

    public CheckOutDTO dtoMapper(Orders order, Payment payment) {
        return new CheckOutDTO(
                order.getOrderId(),
                order.getTransactionId(),
                order.getDate(),
                order.getTime(),
                order.getCustomer(),
                order.getOrderItems(),
                payment.getPaymentMethod(),
                payment.getPaidAt(),
                payment.getStatus(),
                order.getTotalAmount()
        );
    }

    private OrderHistoryDTO toOrderHistoryDTO(Orders order) {
        Payment payment = order.getPayment().stream().findFirst().orElse(null);
        List<OrderItemHistoryDTO> items = order.getOrderItems().stream()
                .map(item -> new OrderItemHistoryDTO(
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getQuantity(),
                        item.getPriceAtCheckout()
                ))
                .toList();

        return new OrderHistoryDTO(
                order.getOrderId(),
                order.getTransactionId(),
                order.getDate(),
                order.getTime(),
                order.getTotalAmount(),
                payment == null ? null : payment.getPaymentMethod(),
                payment == null ? null : payment.getPaidAt(),
                payment == null ? null : payment.getStatus(),
                items
        );
    }

    private void logOrder(CheckOutDTO checkOutDTO) {
        System.out.println(
                "\n" +
                        "==================================================\n" +
                        "                    ORDER\n" +
                        "==================================================\n" +
                        " Order ID        : " + checkOutDTO.getOrderId() + "\n" +
                        " Transaction ID  : " + checkOutDTO.getTransactionId() + "\n" +
                        " Customer ID     : " + checkOutDTO.getCustomer().getCustomerId() + "\n" +
                        " Date            : " + checkOutDTO.getDate() + "\n" +
                        " Time            : " + checkOutDTO.getTime() + "\n" +
                        " Payment Method  : " + checkOutDTO.getPaymentMethod() + "\n" +
                        " Paid At         : " + checkOutDTO.getPaidAt() + "\n" +
                        " Status          : " + checkOutDTO.getStatus() + "\n" +
                        " Items           : " + checkOutDTO.getOrderItems().size() + "\n" +
                        "==================================================\n" +
                        "          Thank you for shopping with us!\n" +
                        "                 Visit us again :)\n" +
                        "==================================================\n"
        );
    }

    private void requireCustomerAccess(Customer customer, UserDetails currentUser) {
        boolean isManager = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MANAGER"::equals);
        if (!isManager && (customer.getEmail() == null || !customer.getEmail().equalsIgnoreCase(currentUser.getUsername()))) {
            throw new NotFoundException("Customer not found");
        }
    }
}
