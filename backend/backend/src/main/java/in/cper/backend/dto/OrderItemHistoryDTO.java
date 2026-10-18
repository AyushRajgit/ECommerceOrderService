package in.cper.backend.dto;

import java.math.BigDecimal;

public record OrderItemHistoryDTO(
        int productId,
        String productName,
        int quantity,
        BigDecimal lineTotal
) {
}
