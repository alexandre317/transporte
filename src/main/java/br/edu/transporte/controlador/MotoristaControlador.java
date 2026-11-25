package br.edu.transporte.controlador;

import br.edu.transporte.dto.requisicao.MotoristaAtualizacaoRequisicao;
import br.edu.transporte.dto.requisicao.MotoristaCadastroRequisicao;
import br.edu.transporte.dto.resposta.MotoristaAtualizacaoResposta;
import br.edu.transporte.dto.resposta.MotoristaCadastroResposta;
import br.edu.transporte.dto.resposta.MotoristaConsultaResposta;
import br.edu.transporte.mapeador.MotoristaMapeador;
import br.edu.transporte.servico.MotoristaServico;
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
@RequestMapping("v1/motoristas")
public class MotoristaControlador {

    private final MotoristaServico servico;

    private final MotoristaMapeador mapeador;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Lista motoristas ou busca um por id")
    public ResponseEntity<List<MotoristaConsultaResposta>> listarOuBuscarPorId(@RequestParam(required = false) Long id) {
        log.debug("requisição para listar motoristas, parâmetro id: '{}'", id);

        var motoristas = servico.listar(id);

        var respostas = mapeador.paraListaConsultaResposta(motoristas);

        return ResponseEntity.ok(respostas);
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Motorista cadastrado")
    public ResponseEntity<MotoristaCadastroResposta> cadastrar(@Valid @RequestBody MotoristaCadastroRequisicao requisicao) {
        log.debug("requisição para cadastrar motorista");

        var paraSalvar = mapeador.paraMotorista(requisicao);

        var salvo = servico.salvar(paraSalvar);

        var resposta = mapeador.paraCadastroResposta(salvo);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("{id}")
    @ApiResponse(responseCode = "200", description = "Motorista atualizado")
    public ResponseEntity<MotoristaAtualizacaoResposta> atualizar(@Valid @RequestBody MotoristaAtualizacaoRequisicao requisicao,
                                                                  @PathVariable Long id) {
        log.debug("requisição para atualizar motorista com id: '{}'", id);

        var paraAtualizar = mapeador.paraMotorista(requisicao);

        var atualizado = servico.atualizar(paraAtualizar, id);

        var resposta = mapeador.paraAtualizacaoResposta(atualizado);

        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "204", description = "Motorista excluído")
    public ResponseEntity<Void> excluirPorId(@PathVariable Long id) {
        log.debug("requisição para excluir motorista com id: '{}'", id);

        var paraExcluir = servico.buscarMotoristaPorIdOuLancarErro(id);

        servico.excluir(paraExcluir);

        return ResponseEntity.noContent().build();
    }

}
