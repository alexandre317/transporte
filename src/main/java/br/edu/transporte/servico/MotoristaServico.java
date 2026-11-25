package br.edu.transporte.servico;

import br.edu.transporte.excecao.CnhJaCadastradaException;
import br.edu.transporte.excecao.ConflitoDadosException;
import br.edu.transporte.excecao.NaoEncontradoException;
import br.edu.transporte.modelo.Motorista;
import br.edu.transporte.repositorio.MotoristaRepositorio;
import br.edu.transporte.repositorio.ViagemRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MotoristaServico {

    private final MotoristaRepositorio motoristaRepositorio;
    private final ViagemRepositorio viagemRepositorio;

    public List<Motorista> listar(Long id) {
        return id == null ? motoristaRepositorio.findAll() : Collections.singletonList(buscarMotoristaPorIdOuLancarErro(id));
    }

    public Motorista buscarMotoristaPorIdOuLancarErro(Long id) {
        return motoristaRepositorio.findById(id)
                .orElseThrow(() -> new NaoEncontradoException(
                        String.format("Motorista com id %d não foi encontrado.", id)
                ));
    }

    public long contar() {
        return motoristaRepositorio.count();
    }

    @Transactional
    public Motorista salvar(Motorista motoristaParaSalvar) {
        motoristaParaSalvar.setCnh(motoristaParaSalvar.getCnh().trim());

        garantirQueCnhNaoExiste(motoristaParaSalvar.getCnh());

        return motoristaRepositorio.save(motoristaParaSalvar);
    }

    @Transactional
    public Motorista atualizar(Motorista motoristaParaAtualizar, Long id) {
        var existente = buscarMotoristaPorIdOuLancarErro(id);
        motoristaParaAtualizar.setId(id);
        motoristaParaAtualizar.herdarCriacao(existente);
        motoristaParaAtualizar.setCnh(motoristaParaAtualizar.getCnh().trim());

        garantirQueCnhNaoExiste(motoristaParaAtualizar.getCnh(), id);

        return motoristaRepositorio.save(motoristaParaAtualizar);
    }

    @Transactional
    public void excluir(Motorista motorista) {
        var motoristaParaExcluir = buscarMotoristaPorIdOuLancarErro(motorista.getId());

        var viagens = viagemRepositorio.contarPorMotorista(motoristaParaExcluir.getId());

        if (viagens > 0) {
            throw new ConflitoDadosException(String.format(
                    "Não é possível excluir o motorista '%s' (id %d): ele está vinculado a %d viagem(ns). "
                            + "Exclua ou reatribua essas viagens antes.",
                    motoristaParaExcluir.getNome(), motoristaParaExcluir.getId(), viagens));
        }

        motoristaRepositorio.delete(motoristaParaExcluir);
    }

    private void garantirQueCnhNaoExiste(String cnh) {
        if (motoristaRepositorio.contarPorCnh(cnh) > 0) {
            throw new CnhJaCadastradaException(
                    String.format("Já existe um motorista cadastrado com a CNH '%s'.", cnh)
            );
        }
    }

    private void garantirQueCnhNaoExiste(String cnh, Long id) {
        if (motoristaRepositorio.contarPorCnhDeOutroMotorista(cnh, id) > 0) {
            throw new CnhJaCadastradaException(
                    String.format("A CNH '%s' já pertence a outro motorista; não é possível usá-la neste (id %d).", cnh, id)
            );
        }
    }

}
