package br.edu.transporte.controlador;

import br.edu.transporte.dto.requisicao.ManutencaoCadastroRequisicao;
import br.edu.transporte.dto.resposta.ManutencaoCadastroResposta;
import br.edu.transporte.dto.resposta.ManutencaoConsultaResposta;
import br.edu.transporte.dto.resposta.PaginaResposta;
import br.edu.transporte.enums.SituacaoManutencaoEnum;
import br.edu.transporte.exportacao.RelatorioServico;
import br.edu.transporte.mapeador.ManutencaoMapeador;
import br.edu.transporte.servico.ManutencaoServico;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@Log4j2
@RestController
@RequestMapping("v1/manutencoes")
public class ManutencaoControlador {

    private final ManutencaoServico servico;

    private final RelatorioServico relatorioServico;

    private final ManutencaoMapeador mapeador;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Lista manutenções com filtros e paginação")
    public ResponseEntity<PaginaResposta<ManutencaoConsultaResposta>> listar(
            @RequestParam(required = false) SituacaoManutencaoEnum situacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        log.debug("requisição para listar manutenções, situacao: '{}', inicio: '{}', fim: '{}'", situacao, inicio, fim);

        var manutencoes = servico.pesquisar(situacao, inicio, fim, pagina, tamanho);

        var resposta = PaginaResposta.de(manutencoes.map(mapeador::paraConsultaResposta));

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("{id}")
    @ApiResponse(responseCode = "200", description = "Busca uma manutenção pelo id")
    public ResponseEntity<ManutencaoConsultaResposta> buscarPorId(@PathVariable Long id) {
        log.debug("requisição para buscar manutenção com id: '{}'", id);

        var manutencao = servico.buscarManutencaoPorIdOuLancarErro(id);

        return ResponseEntity.ok(mapeador.paraConsultaResposta(manutencao));
    }

    @GetMapping("exportar")
    @ApiResponse(responseCode = "200", description = "Exporta manutenções em CSV ou PDF")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(defaultValue = "csv") String formato,
            @RequestParam(required = false) SituacaoManutencaoEnum situacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        log.debug("requisição para exportar manutenções, formato: '{}'", formato);

        var manutencoes = servico.filtrar(situacao, inicio, fim);

        return relatorioServico.gerarManutencoes(formato, manutencoes);
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Manutenção cadastrada")
    public ResponseEntity<ManutencaoCadastroResposta> cadastrar(@Valid @RequestBody ManutencaoCadastroRequisicao requisicao) {
        log.debug("requisição para cadastrar manutenção");

        var paraSalvar = mapeador.paraManutencao(requisicao);

        var salva = servico.salvar(paraSalvar);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapeador.paraCadastroResposta(salva));
    }

}
