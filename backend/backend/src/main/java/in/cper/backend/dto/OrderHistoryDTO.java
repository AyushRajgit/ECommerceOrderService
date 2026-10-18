package in.cper.backend.dto;

import in.cper.backend.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record OrderHistoryDTO(
        int orderId,
        String transactionId,
        LocalDate date,
        LocalTime time,
        BigDecimal totalAmount,
        String paymentMethod,
        String paidAt,
        Status status,
        List<OrderItemHistoryDTO> items
) {
}
