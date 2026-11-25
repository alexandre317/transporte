package br.edu.transporte.controlador;

import br.edu.transporte.dto.resposta.PainelResposta;
import br.edu.transporte.servico.PainelServico;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Log4j2
@RestController
@RequestMapping("v1/painel")
public class PainelControlador {

    private final PainelServico servico;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Totais, gráfico mensal e alertas de manutenção")
    public ResponseEntity<PainelResposta> obterPainel() {
        log.debug("requisição para obter o painel");

        return ResponseEntity.ok(servico.obterPainel());
    }

}
