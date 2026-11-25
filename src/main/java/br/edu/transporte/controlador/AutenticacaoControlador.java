package br.edu.transporte.controlador;

import br.edu.transporte.dto.requisicao.EntrarRequisicao;
import br.edu.transporte.dto.resposta.EntrarResposta;
import br.edu.transporte.servico.AutenticacaoServico;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Log4j2
@RestController
@RequestMapping("v1/autenticacao")
public class AutenticacaoControlador {

    private final AutenticacaoServico servico;

    @PostMapping("entrar")
    @ApiResponse(responseCode = "200", description = "Login realizado; retorna o token JWT")
    public ResponseEntity<EntrarResposta> entrar(@Valid @RequestBody EntrarRequisicao requisicao,
                                                 HttpServletRequest requisicaoHttp) {
        log.debug("requisição de login");

        return ResponseEntity.ok(servico.entrar(requisicao, requisicaoHttp.getRemoteAddr()));
    }

}
