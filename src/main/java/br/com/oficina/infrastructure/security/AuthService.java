package br.com.oficina.infrastructure.security;

import br.com.oficina.application.dto.LoginRequest;
import br.com.oficina.application.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Autenticação do usuário administrador (credenciais vindas de configuração/variáveis de ambiente). */
@Service
public class AuthService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final String usuarioAdmin;
    private final String senhaAdminHash;

    public AuthService(JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.security.admin.username}") String usuarioAdmin,
                       @Value("${app.security.admin.password}") String senhaAdmin) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioAdmin = usuarioAdmin;
        this.senhaAdminHash = passwordEncoder.encode(senhaAdmin);
    }

    public TokenResponse autenticar(LoginRequest request) {
        boolean valido = usuarioAdmin.equals(request.username())
                && passwordEncoder.matches(request.password(), senhaAdminHash);
        if (!valido) {
            throw new CredenciaisInvalidasException();
        }
        return new TokenResponse(jwtService.gerar(usuarioAdmin), "Bearer", jwtService.getExpiracaoSegundos());
    }
}
