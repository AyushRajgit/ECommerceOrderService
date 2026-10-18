package in.cper.backend.dto;

import in.cper.backend.entity.Address;

public record CustomerProfileDTO(
        int customerId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Address address
) {
}
