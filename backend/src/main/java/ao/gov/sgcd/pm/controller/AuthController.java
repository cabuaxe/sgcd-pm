package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.config.UserProperties;
import ao.gov.sgcd.pm.dto.AuthRequestDTO;
import ao.gov.sgcd.pm.dto.AuthResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação: login e informações do utilizador autenticado")
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserProperties userProperties;

    @Value("${sgcd-pm.stakeholder.token}")
    private String stakeholderToken;

    @Operation(summary = "Autenticar utilizador", description = "Realiza login com nome de utilizador e palavra-passe, devolvendo um token JWT")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        var matched = userProperties.getUsers().stream()
                .filter(u -> u.getUsername().equals(request.getUsername()))
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
                .findFirst();

        if (matched.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        String role = matched.get().getRole();
        String token = tokenProvider.generateToken(request.getUsername(), role);

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token(token)
                .role(role)
                .expiresIn(tokenProvider.getExpiration())
                .build());
    }

    @Operation(summary = "Obter utilizador autenticado", description = "Devolve o nome de utilizador e o papel (role) do utilizador autenticado")
    @ApiResponse(responseCode = "200", description = "Informações do utilizador devolvidas com sucesso")
    @ApiResponse(responseCode = "401", description = "Utilizador não autenticado")
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        String role = authentication.getAuthorities().iterator().next().getAuthority()
                .replace("ROLE_", "");
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "role", role
        ));
    }
}
