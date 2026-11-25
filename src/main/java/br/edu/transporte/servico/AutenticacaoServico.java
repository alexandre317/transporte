package br.edu.transporte.servico;

import br.edu.transporte.dto.requisicao.EntrarRequisicao;
import br.edu.transporte.dto.resposta.EntrarResposta;
import br.edu.transporte.excecao.CredenciaisInvalidasException;
import br.edu.transporte.repositorio.UsuarioRepositorio;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutenticacaoServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder codificadorDeSenha;
    private final JwtEncoder codificadorJwt;
    private final LimitadorTentativasLogin limitador;

    @Value("${app.jwt.expiracao-minutos}")
    private long expiracaoEmMinutos;

    /** Hash descartável usado para gastar o mesmo tempo quando o e-mail não existe (evita enumerar contas). */
    private String hashFicticio;

    @PostConstruct
    void prepararHashFicticio() {
        hashFicticio = codificadorDeSenha.encode(UUID.randomUUID().toString());
    }

    public EntrarResposta entrar(EntrarRequisicao requisicao, String enderecoIp) {
        var email = requisicao.getEmail().trim();
        var chave = email.toLowerCase() + "|" + enderecoIp;

        limitador.verificarBloqueio(chave);

        var usuario = usuarioRepositorio.buscarPorEmail(email).orElse(null);

        var senhaConfere = codificadorDeSenha.matches(requisicao.getSenha(),
                usuario != null ? usuario.getSenha() : hashFicticio);

        if (usuario == null || !senhaConfere) {
            limitador.registrarFalha(chave);
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos.");
        }

        limitador.limpar(chave);

        var agora = Instant.now();

        var claims = JwtClaimsSet.builder()
                .subject(usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracaoEmMinutos, ChronoUnit.MINUTES))
                .claim("role", usuario.getPerfil().name())
                .claim("email", usuario.getEmail())
                .build();

        var cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        var token = codificadorJwt.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();

        return new EntrarResposta(token, usuario.getId(), usuario.getNome(), usuario.getPerfil().name());
    }

}
