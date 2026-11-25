package br.edu.transporte.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "provedorDeAuditor")
public class AuditoriaConfig {

    private static final String SISTEMA = "sistema";

    @Bean
    public AuditorAware<String> provedorDeAuditor() {
        return () -> {
            var autenticacao = SecurityContextHolder.getContext().getAuthentication();

            if (autenticacao == null || !autenticacao.isAuthenticated()
                    || autenticacao instanceof AnonymousAuthenticationToken) {
                return Optional.of(SISTEMA);
            }

            if (autenticacao.getPrincipal() instanceof Jwt jwt && jwt.getClaimAsString("email") != null) {
                return Optional.of(jwt.getClaimAsString("email"));
            }

            return Optional.of(autenticacao.getName());
        };
    }
}
