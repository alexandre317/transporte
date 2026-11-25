package br.edu.transporte.servico;

import br.edu.transporte.excecao.ConflitoDadosException;
import br.edu.transporte.excecao.NaoEncontradoException;
import br.edu.transporte.excecao.PlacaJaCadastradaException;
import br.edu.transporte.modelo.Veiculo;
import br.edu.transporte.repositorio.ManutencaoRepositorio;
import br.edu.transporte.repositorio.VeiculoRepositorio;
import br.edu.transporte.repositorio.ViagemRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoServico {

    private final VeiculoRepositorio veiculoRepositorio;
    private final ViagemRepositorio viagemRepositorio;
    private final ManutencaoRepositorio manutencaoRepositorio;

    public List<Veiculo> listar(Long id) {
        return id == null ? veiculoRepositorio.findAll() : Collections.singletonList(buscarVeiculoPorIdOuLancarErro(id));
    }

    public Veiculo buscarVeiculoPorIdOuLancarErro(Long id) {
        return veiculoRepositorio.findById(id)
                .orElseThrow(() -> new NaoEncontradoException(
                        String.format("Veículo com id %d não foi encontrado.", id)
                ));
    }

    public long contar() {
        return veiculoRepositorio.count();
    }

    @Transactional
    public Veiculo salvar(Veiculo veiculoParaSalvar) {
        normalizarPlaca(veiculoParaSalvar);

        garantirQuePlacaNaoExiste(veiculoParaSalvar.getPlaca());

        return veiculoRepositorio.save(veiculoParaSalvar);
    }

    @Transactional
    public Veiculo atualizar(Veiculo veiculoParaAtualizar, Long id) {
        var existente = buscarVeiculoPorIdOuLancarErro(id);
        veiculoParaAtualizar.setId(id);
        veiculoParaAtualizar.herdarCriacao(existente);

        normalizarPlaca(veiculoParaAtualizar);

        garantirQuePlacaNaoExiste(veiculoParaAtualizar.getPlaca(), id);

        return veiculoRepositorio.save(veiculoParaAtualizar);
    }

    @Transactional
    public void excluir(Veiculo veiculo) {
        var veiculoParaExcluir = buscarVeiculoPorIdOuLancarErro(veiculo.getId());

        var viagens = viagemRepositorio.contarPorVeiculo(veiculoParaExcluir.getId());
        var manutencoes = manutencaoRepositorio.contarPorVeiculo(veiculoParaExcluir.getId());

        if (viagens > 0 || manutencoes > 0) {
            throw new ConflitoDadosException(String.format(
                    "Não é possível excluir o veículo '%s' (id %d): ele está vinculado a %d viagem(ns) e %d manutenção(ões). "
                            + "Exclua ou reatribua esses registros antes, ou apenas inative o veículo.",
                    veiculoParaExcluir.getPlaca(), veiculoParaExcluir.getId(), viagens, manutencoes));
        }

        veiculoRepositorio.delete(veiculoParaExcluir);
    }

    @Transactional
    public void atualizarQuilometragemAtual(Veiculo veiculo, Long quilometragem) {
        veiculo.setQuilometragemAtual(quilometragem);

        veiculoRepositorio.save(veiculo);
    }

    private void normalizarPlaca(Veiculo veiculo) {
        veiculo.setPlaca(veiculo.getPlaca().trim().toUpperCase());
    }

    private void garantirQuePlacaNaoExiste(String placa) {
        if (veiculoRepositorio.contarPorPlaca(placa) > 0) {
            throw new PlacaJaCadastradaException(
                    String.format("Já existe um veículo cadastrado com a placa '%s'.", placa)
            );
        }
    }

    private void garantirQuePlacaNaoExiste(String placa, Long id) {
        if (veiculoRepositorio.contarPorPlacaDeOutroVeiculo(placa, id) > 0) {
            throw new PlacaJaCadastradaException(
                    String.format("A placa '%s' já pertence a outro veículo; não é possível usá-la neste (id %d).", placa, id)
            );
        }
    }

}
