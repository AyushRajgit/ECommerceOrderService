package in.cper.backend.service;

import in.cper.backend.entity.Orders;
import in.cper.backend.entity.Payment;
import in.cper.backend.entity.Status;
import in.cper.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class SimulatedPaymentService {

    private final PaymentRepository paymentRepository;

    public SimulatedPaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment simulate(Orders order, String paymentMethod) {
        String displayMethod = switch (paymentMethod == null ? "" : paymentMethod.trim().toUpperCase(Locale.ROOT)) {
            case "SIMULATED_UPI" -> "Simulated UPI";
            case "SIMULATED_CARD" -> "Simulated Card";
            default -> throw new IllegalArgumentException("Choose a supported simulated payment method");
        };

        Payment payment = new Payment(
                "SIM-" + UUID.randomUUID(),
                displayMethod,
                LocalDate.now(),
                LocalTime.now(),
                "Shopnest Demo Payment Service",
                Status.SUCCESS,
                order
        );

        return paymentRepository.save(payment);
    }
}
