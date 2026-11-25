package br.edu.transporte.controlador;

import br.edu.transporte.dto.requisicao.MinhaContaAtualizacaoRequisicao;
import br.edu.transporte.dto.resposta.UsuarioAtualizacaoResposta;
import br.edu.transporte.dto.resposta.UsuarioConsultaResposta;
import br.edu.transporte.mapeador.UsuarioMapeador;
import br.edu.transporte.servico.UsuarioServico;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Log4j2
@RestController
@RequestMapping("v1/minha-conta")
public class MinhaContaControlador {

    private final UsuarioServico servico;

    private final UsuarioMapeador mapeador;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Dados da conta autenticada")
    public ResponseEntity<UsuarioConsultaResposta> obterConta(@AuthenticationPrincipal Jwt jwt) {
        log.debug("requisição para obter a própria conta");

        var usuario = servico.buscarUsuarioPorIdOuLancarErro(Long.valueOf(jwt.getSubject()));

        return ResponseEntity.ok(mapeador.paraConsultaResposta(usuario));
    }

    @PutMapping
    @ApiResponse(responseCode = "200", description = "Nome, e-mail e/ou senha da própria conta atualizados")
    public ResponseEntity<UsuarioAtualizacaoResposta> atualizarConta(@Valid @RequestBody MinhaContaAtualizacaoRequisicao requisicao,
                                                                     @AuthenticationPrincipal Jwt jwt) {
        log.debug("requisição para atualizar a própria conta");

        var dados = mapeador.paraUsuario(requisicao);

        var atualizado = servico.atualizarConta(Long.valueOf(jwt.getSubject()), dados,
                requisicao.getSenhaAtual(), requisicao.getNovaSenha(), requisicao.getConfirmarNovaSenha());

        return ResponseEntity.ok(mapeador.paraAtualizacaoResposta(atualizado));
    }

}
