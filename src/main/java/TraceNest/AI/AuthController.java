
package TraceNest.AI;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record RegisterRequest(
            @NotBlank(message = "Email is required.")
            @Email(message = "Enter a valid email address.")
            @Size(max = 254, message = "Email must not exceed 254 characters.")
            String email,

            @NotBlank(message = "Password is required.")
            @Size(min = 8, max = 72,
                  message = "Password must contain between 8 and 72 characters.")
            String password
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required.")
            @Email(message = "Enter a valid email address.")
            @Size(max = 254, message = "Email must not exceed 254 characters.")
            String email,

            @NotBlank(message = "Password is required.")
            @Size(max = 72, message = "Password must not exceed 72 characters.")
            String password
    ) {}

    @PostMapping("/register")
    public Map<String, String> register(
            @Valid @RequestBody RegisterRequest request) {

        try {
            String email = authService.register(
                    request.email(),
                    request.password()
            );

            return Map.of(
                    "message", "Account created successfully.",
                    "email", email
            );

        } catch (AccountAlreadyExistsException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists."
            );

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        }
    }

    @PostMapping("/login")
    public Map<String, String> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            String token = authService.login(
                    request.email(),
                    request.password()
            );

            return Map.of("token", token);

        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password."
            );
        }
    }
}
