package in.cper.backend.dto;

import in.cper.backend.entity.Product;

import java.util.List;

public record ProductPageDTO(
        List<Product> products,
        int pageNo,
        int pageSize,
        long totalElements,
        int totalPages
) {
}
