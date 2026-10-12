
package TraceNest.AI;

import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String register(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        validatePassword(rawPassword);

        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new AccountAlreadyExistsException();
        }

        AppUser user = new AppUser(
                normalizedEmail,
                passwordEncoder.encode(rawPassword));

        try {
            AppUser savedUser = appUserRepository.saveAndFlush(user);
            return savedUser.getEmail();
        } catch (DataIntegrityViolationException ex) {
            // Protect against simultaneous registrations for the same email.
            throw new AccountAlreadyExistsException();
        }
    }

    @Transactional(readOnly = true)
    public String login(String email, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BadCredentialsException(
                    "Invalid email or password.");
        }

        Optional<AppUser> userOptional =
                appUserRepository.findByEmail(normalizeEmail(email));

        AppUser user = userOptional.orElseThrow(
                () -> new BadCredentialsException(
                        "Invalid email or password."));

        if (!passwordEncoder.matches(
                rawPassword, user.getPassword())) {
            throw new BadCredentialsException(
                    "Invalid email or password.");
        }

        return jwtService.generateToken(user.getEmail());
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String rawPassword) {
        if (rawPassword == null
                || rawPassword.length() < 8
                || rawPassword.length() > 72) {
            throw new IllegalArgumentException(
                    "Password must contain between 8 and 72 characters.");
        }
    }
}
