package br.edu.transporte.controlador;

import br.edu.transporte.dto.requisicao.UsuarioCadastroRequisicao;
import br.edu.transporte.dto.resposta.UsuarioCadastroResposta;
import br.edu.transporte.dto.resposta.UsuarioConsultaResposta;
import br.edu.transporte.mapeador.UsuarioMapeador;
import br.edu.transporte.servico.UsuarioServico;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Log4j2
@RestController
@RequestMapping("v1/usuarios")
public class UsuarioControlador {

    private final UsuarioServico servico;

    private final UsuarioMapeador mapeador;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Lista usuários ou busca um por id (somente administrador)")
    public ResponseEntity<List<UsuarioConsultaResposta>> listarOuBuscarPorId(@RequestParam(required = false) Long id) {
        log.debug("requisição para listar usuários, parâmetro id: '{}'", id);

        var usuarios = servico.listar(id);

        return ResponseEntity.ok(mapeador.paraListaConsultaResposta(usuarios));
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Conta criada (somente administrador)")
    public ResponseEntity<UsuarioCadastroResposta> cadastrar(@Valid @RequestBody UsuarioCadastroRequisicao requisicao) {
        log.debug("requisição para cadastrar usuário");

        var paraSalvar = mapeador.paraUsuario(requisicao);

        var salvo = servico.cadastrar(paraSalvar);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapeador.paraCadastroResposta(salvo));
    }

    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "204", description = "Conta excluída (somente administrador)")
    public ResponseEntity<Void> excluirPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.debug("requisição para excluir usuário com id: '{}'", id);

        var paraExcluir = servico.buscarUsuarioPorIdOuLancarErro(id);

        servico.excluir(paraExcluir, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.noContent().build();
    }

}
