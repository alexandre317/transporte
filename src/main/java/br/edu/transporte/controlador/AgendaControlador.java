package br.edu.transporte.controlador;

import br.edu.transporte.dto.resposta.ViagemConsultaResposta;
import br.edu.transporte.enums.StatusViagemEnum;
import br.edu.transporte.mapeador.ViagemMapeador;
import br.edu.transporte.servico.ViagemServico;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Log4j2
@RestController
@RequestMapping("v1/agenda")
public class AgendaControlador {

    private final ViagemServico servico;

    private final ViagemMapeador mapeador;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Agenda diária de viagens (padrão: hoje)")
    public ResponseEntity<List<ViagemConsultaResposta>> buscarAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Long motoristaId,
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) StatusViagemEnum status) {
        log.debug("requisição para buscar agenda, data: '{}'", data);

        var viagens = servico.buscarAgenda(data, motoristaId, veiculoId, status);

        return ResponseEntity.ok(mapeador.paraListaConsultaResposta(viagens));
    }

}
