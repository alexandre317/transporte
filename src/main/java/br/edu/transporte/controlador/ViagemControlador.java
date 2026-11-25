package br.edu.transporte.controlador;

import br.edu.transporte.dto.requisicao.ViagemAtualizacaoRequisicao;
import br.edu.transporte.dto.requisicao.ViagemCadastroRequisicao;
import br.edu.transporte.dto.resposta.PaginaResposta;
import br.edu.transporte.dto.resposta.ViagemAtualizacaoResposta;
import br.edu.transporte.dto.resposta.ViagemCadastroResposta;
import br.edu.transporte.dto.resposta.ViagemConsultaResposta;
import br.edu.transporte.enums.StatusViagemEnum;
import br.edu.transporte.exportacao.RelatorioServico;
import br.edu.transporte.mapeador.ViagemMapeador;
import br.edu.transporte.servico.ViagemServico;
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
@RequestMapping("v1/viagens")
public class ViagemControlador {

    private final ViagemServico servico;

    private final RelatorioServico relatorioServico;

    private final ViagemMapeador mapeador;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Lista viagens com filtros e paginação")
    public ResponseEntity<PaginaResposta<ViagemConsultaResposta>> listar(
            @RequestParam(required = false) StatusViagemEnum status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        log.debug("requisição para listar viagens, status: '{}', inicio: '{}', fim: '{}'", status, inicio, fim);

        var viagens = servico.pesquisar(status, inicio, fim, pagina, tamanho);

        var resposta = PaginaResposta.de(viagens.map(mapeador::paraConsultaResposta));

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("{id}")
    @ApiResponse(responseCode = "200", description = "Busca uma viagem pelo id")
    public ResponseEntity<ViagemConsultaResposta> buscarPorId(@PathVariable Long id) {
        log.debug("requisição para buscar viagem com id: '{}'", id);

        var viagem = servico.buscarViagemPorIdOuLancarErro(id);

        return ResponseEntity.ok(mapeador.paraConsultaResposta(viagem));
    }

    @GetMapping("exportar")
    @ApiResponse(responseCode = "200", description = "Exporta viagens em CSV ou PDF")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(defaultValue = "csv") String formato,
            @RequestParam(required = false) StatusViagemEnum status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        log.debug("requisição para exportar viagens, formato: '{}'", formato);

        var viagens = servico.filtrar(status, inicio, fim);

        return relatorioServico.gerarViagens(formato, viagens);
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Viagem cadastrada")
    public ResponseEntity<ViagemCadastroResposta> cadastrar(@Valid @RequestBody ViagemCadastroRequisicao requisicao) {
        log.debug("requisição para cadastrar viagem");

        var paraSalvar = mapeador.paraViagem(requisicao);

        var salva = servico.salvar(paraSalvar);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapeador.paraCadastroResposta(salva));
    }

    @PutMapping("{id}")
    @ApiResponse(responseCode = "200", description = "Viagem atualizada")
    public ResponseEntity<ViagemAtualizacaoResposta> atualizar(@Valid @RequestBody ViagemAtualizacaoRequisicao requisicao,
                                                               @PathVariable Long id) {
        log.debug("requisição para atualizar viagem com id: '{}'", id);

        var paraAtualizar = mapeador.paraViagem(requisicao);

        var atualizada = servico.atualizar(paraAtualizar, id);

        return ResponseEntity.ok(mapeador.paraAtualizacaoResposta(atualizada));
    }

    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "204", description = "Viagem excluída")
    public ResponseEntity<Void> excluirPorId(@PathVariable Long id) {
        log.debug("requisição para excluir viagem com id: '{}'", id);

        var paraExcluir = servico.buscarViagemPorIdOuLancarErro(id);

        servico.excluir(paraExcluir);

        return ResponseEntity.noContent().build();
    }

}
