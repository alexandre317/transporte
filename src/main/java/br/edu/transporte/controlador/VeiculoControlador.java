package br.edu.transporte.controlador;

import br.edu.transporte.dto.requisicao.VeiculoAtualizacaoRequisicao;
import br.edu.transporte.dto.requisicao.VeiculoCadastroRequisicao;
import br.edu.transporte.dto.resposta.VeiculoAtualizacaoResposta;
import br.edu.transporte.dto.resposta.VeiculoCadastroResposta;
import br.edu.transporte.dto.resposta.VeiculoConsultaResposta;
import br.edu.transporte.mapeador.VeiculoMapeador;
import br.edu.transporte.servico.VeiculoServico;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Log4j2
@RestController
@RequestMapping("v1/veiculos")
public class VeiculoControlador {

    private final VeiculoServico servico;

    private final VeiculoMapeador mapeador;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Lista veiculos ou busca um por id")
    public ResponseEntity<List<VeiculoConsultaResposta>> listarOuBuscarPorId(@RequestParam(required = false) Long id) {
        log.debug("requisição para listar veiculos, parâmetro id: '{}'", id);

        var veiculos = servico.listar(id);

        var respostas = mapeador.paraListaConsultaResposta(veiculos);

        return ResponseEntity.ok(respostas);
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Veículo cadastrado")
    public ResponseEntity<VeiculoCadastroResposta> cadastrar(@Valid @RequestBody VeiculoCadastroRequisicao requisicao) {
        log.debug("requisição para cadastrar veiculo");

        var paraSalvar = mapeador.paraVeiculo(requisicao);

        var salvo = servico.salvar(paraSalvar);

        var resposta = mapeador.paraCadastroResposta(salvo);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("{id}")
    @ApiResponse(responseCode = "200", description = "Veículo atualizado")
    public ResponseEntity<VeiculoAtualizacaoResposta> atualizar(@Valid @RequestBody VeiculoAtualizacaoRequisicao requisicao,
                                                                @PathVariable Long id) {
        log.debug("requisição para atualizar veiculo com id: '{}'", id);

        var paraAtualizar = mapeador.paraVeiculo(requisicao);

        var atualizado = servico.atualizar(paraAtualizar, id);

        var resposta = mapeador.paraAtualizacaoResposta(atualizado);

        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "204", description = "Veículo excluído")
    public ResponseEntity<Void> excluirPorId(@PathVariable Long id) {
        log.debug("requisição para excluir veiculo com id: '{}'", id);

        var paraExcluir = servico.buscarVeiculoPorIdOuLancarErro(id);

        servico.excluir(paraExcluir);

        return ResponseEntity.noContent().build();
    }

}
