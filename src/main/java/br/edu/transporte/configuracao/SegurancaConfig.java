package br.edu.transporte.configuracao;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import br.edu.transporte.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SegurancaConfig {

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    @Value("${app.jwt.segredo}")
    private String segredoJwt;

    @Value("${app.cors.origem-permitida}")
    private String origemPermitida;

    @Bean
    public SecurityFilterChain cadeiaDeFiltros(HttpSecurity http, UsuarioRepositorio usuarioRepositorio) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(pontoDeEntrada())
                        .accessDeniedHandler(acessoNegado()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/favicon.ico", "/app.css", "/app.js").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/autenticacao/entrar").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v1/usuarios/**", "/v1/manutencoes/**").hasRole("ADMIN")
                        .requestMatchers("/v1/minha-conta").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/**").authenticated()
                        .anyRequest().hasRole("ADMIN"))
                .oauth2ResourceServer(oauth -> oauth
                        .authenticationEntryPoint(pontoDeEntrada())
                        .accessDeniedHandler(acessoNegado())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDeJwt(usuarioRepositorio))));

        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder codificadorJwt() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveSecreta().getEncoded()));
    }

    @Bean
    public JwtDecoder decodificadorJwt() {
        return NimbusJwtDecoder.withSecretKey(chaveSecreta())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * A cada requisição confere no banco se a conta ainda existe e usa o perfil ATUAL dela.
     * Assim, uma conta excluída pelo administrador perde o acesso imediatamente, sem esperar o token expirar.
     */
    private Converter<Jwt, JwtAuthenticationToken> conversorDeJwt(UsuarioRepositorio usuarioRepositorio) {
        return jwt -> {
            Long usuarioId;
            try {
                usuarioId = Long.valueOf(jwt.getSubject());
            } catch (NumberFormatException e) {
                throw new BadCredentialsException("Token inválido: identificação do usuário malformada.");
            }

            var usuario = usuarioRepositorio.findById(usuarioId)
                    .orElseThrow(() -> new BadCredentialsException("Conta inexistente: o usuário do token foi removido."));

            return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name())));
        };
    }

    @Bean
    public CorsConfigurationSource configuracaoCors() {
        var configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(Arrays.stream(origemPermitida.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList());
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuracao.setExposedHeaders(List.of("Content-Disposition"));

        var fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);

        return fonte;
    }

    private AuthenticationEntryPoint pontoDeEntrada() {
        return (requisicao, resposta, excecao) -> EscritorErroJson.escrever(requisicao, resposta, 401,
                "Não autenticado", mensagemDeAutenticacao(excecao));
    }

    private AccessDeniedHandler acessoNegado() {
        return (requisicao, resposta, excecao) -> EscritorErroJson.escrever(requisicao, resposta, 403, "Acesso negado",
                String.format("Acesso negado: a operação %s %s é restrita a administradores e o seu perfil não tem permissão.",
                        requisicao.getMethod(), requisicao.getRequestURI()));
    }

    private String mensagemDeAutenticacao(AuthenticationException excecao) {
        var texto = excecao.getMessage() == null ? "" : excecao.getMessage().toLowerCase();

        if (texto.contains("expired")) {
            return "O token de acesso expirou. Faça login novamente em POST /v1/autenticacao/entrar.";
        }
        if (texto.contains("conta inexistente")) {
            return "A conta associada a este token não existe mais. Faça login com outra conta.";
        }
        if (texto.contains("jwt") || texto.contains("token") || texto.contains("signature")) {
            return "Token de acesso inválido (assinatura ou formato incorretos). Faça login novamente.";
        }
        return "Autenticação necessária: envie o token no cabeçalho 'Authorization: Bearer <token>' "
                + "obtido em POST /v1/autenticacao/entrar.";
    }

    private SecretKey chaveSecreta() {
        var bytes = segredoJwt == null ? new byte[0] : segredoJwt.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "O segredo do JWT (variável JWT_SEGREDO) deve ter pelo menos " + TAMANHO_MINIMO_SEGREDO + " caracteres.");
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

}
