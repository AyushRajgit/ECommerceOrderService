package in.cper.backend.controller;

import in.cper.backend.dto.LoginRequestDTO;
import in.cper.backend.dto.RegisterRequestDTO;
import in.cper.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/amazon/auth")
public class AuthController {

    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        authService.registerNewUser(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("User Registered Successful!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        authService.login(loginRequestDTO, request);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully Logged In!");
    }
}
