package in.cper.backend.controller;

import in.cper.backend.dto.CheckOutDTO;
import in.cper.backend.dto.OrderHistoryDTO;
import in.cper.backend.service.CheckOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/amazon/checkout")
public class CheckOutController {

    private CheckOutService checkOutService;

    @Autowired
    public CheckOutController(CheckOutService checkOutService) {
        this.checkOutService = checkOutService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER')")
    public ResponseEntity<CheckOutDTO> checkOut(
            @RequestParam int customerId,
            @RequestParam(defaultValue = "SIMULATED_UPI") String paymentMethod,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        CheckOutDTO checkOutDTO = checkOutService.performCheckOut(customerId, currentUser, paymentMethod);
        return ResponseEntity.status(HttpStatus.OK).body(checkOutDTO);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('USER','MANAGER')")
    public ResponseEntity<List<OrderHistoryDTO>> getMyOrders(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(checkOutService.getMyOrders(currentUser.getUsername()));
    }
}
