package in.cper.backend.service;

import in.cper.backend.dto.LoginRequestDTO;
import in.cper.backend.dto.RegisterRequestDTO;
import in.cper.backend.entity.CustomUserDetailsEntity;
import in.cper.backend.exceptions.UserAlreadyExists;
import in.cper.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final String default_Role = "ROLE_USER";
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerNewUser(RegisterRequestDTO registerRequestDTO) {
        Optional<CustomUserDetailsEntity> user = userRepository.findByEmail(registerRequestDTO.getEmail());
        if (user.isPresent()) {
            throw new UserAlreadyExists("User with email : " + registerRequestDTO.getEmail() + " already exists, user another email to create account");
        }

        CustomUserDetailsEntity newUser = new CustomUserDetailsEntity();
        newUser.setEmail(registerRequestDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        newUser.setRole(default_Role);

        userRepository.save(newUser);
    }

    public void login(LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        Authentication unAuthUser = new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
        Authentication authUser = authenticationManager.authenticate(unAuthUser);

        SecurityContextHolder.getContext().setAuthentication(authUser);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }
}
