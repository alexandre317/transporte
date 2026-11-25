package br.edu.transporte.servico;

import br.edu.transporte.enums.StatusViagemEnum;
import br.edu.transporte.excecao.NaoEncontradoException;
import br.edu.transporte.excecao.NegocioException;
import br.edu.transporte.modelo.Viagem;
import br.edu.transporte.regra.RegraAgenda;
import br.edu.transporte.repositorio.ViagemRepositorio;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViagemServico {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final ViagemRepositorio viagemRepositorio;
    private final VeiculoServico veiculoServico;
    private final MotoristaServico motoristaServico;
    private final List<RegraAgenda> regrasAgenda;

    public Page<Viagem> pesquisar(StatusViagemEnum status, LocalDate inicio, LocalDate fim, int pagina, int tamanho) {
        validarPeriodo(inicio, fim);

        var paginacao = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO_PAGINA),
                Sort.by(Sort.Direction.DESC, "dataHora"));

        return viagemRepositorio.findAll(montarFiltro(status, inicio, fim), paginacao);
    }

    public List<Viagem> filtrar(StatusViagemEnum status, LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);

        return viagemRepositorio.findAll(montarFiltro(status, inicio, fim), Sort.by(Sort.Direction.DESC, "dataHora"));
    }

    public List<Viagem> buscarAgenda(LocalDate data, Long motoristaId, Long veiculoId, StatusViagemEnum status) {
        var dia = data == null ? LocalDate.now() : data;

        return viagemRepositorio.listarPorPeriodo(dia.atStartOfDay(), dia.atTime(23, 59, 59))
                .stream()
                .filter(v -> motoristaId == null || v.getMotorista().getId().equals(motoristaId))
                .filter(v -> veiculoId == null || v.getVeiculo().getId().equals(veiculoId))
                .filter(v -> status == null || v.getStatus() == status)
                .toList();
    }

    public Viagem buscarViagemPorIdOuLancarErro(Long id) {
        return viagemRepositorio.findById(id)
                .orElseThrow(() -> new NaoEncontradoException(
                        String.format("Viagem com id %d não foi encontrada.", id)
                ));
    }

    public long contar() {
        return viagemRepositorio.count();
    }

    public long contarPorStatus(StatusViagemEnum status) {
        return viagemRepositorio.contarPorStatus(status);
    }

    public BigDecimal faturamentoConcluido() {
        return viagemRepositorio.somarValorPorStatus(StatusViagemEnum.CONCLUIDA);
    }

    public List<Long> viagensPorMes() {
        var ano = LocalDate.now().getYear();
        var contagem = new long[12];

        viagemRepositorio.listarPorPeriodo(
                        LocalDate.of(ano, 1, 1).atStartOfDay(),
                        LocalDate.of(ano, 12, 31).atTime(23, 59, 59))
                .forEach(v -> contagem[v.getDataHora().getMonthValue() - 1]++);

        var dados = new ArrayList<Long>();
        for (var c : contagem) {
            dados.add(c);
        }

        return dados;
    }

    @Transactional
    public Viagem salvar(Viagem viagemParaSalvar) {
        resolverRelacionamentos(viagemParaSalvar);

        validar(viagemParaSalvar);

        return viagemRepositorio.save(viagemParaSalvar);
    }

    @Transactional
    public Viagem atualizar(Viagem viagemParaAtualizar, Long id) {
        var existente = buscarViagemPorIdOuLancarErro(id);
        viagemParaAtualizar.setId(id);
        viagemParaAtualizar.herdarCriacao(existente);

        resolverRelacionamentos(viagemParaAtualizar);

        validar(viagemParaAtualizar);

        return viagemRepositorio.save(viagemParaAtualizar);
    }

    @Transactional
    public void excluir(Viagem viagem) {
        var viagemParaExcluir = buscarViagemPorIdOuLancarErro(viagem.getId());

        viagemRepositorio.delete(viagemParaExcluir);
    }

    private void resolverRelacionamentos(Viagem viagem) {
        if (viagem.getVeiculo() == null || viagem.getVeiculo().getId() == null) {
            throw new NegocioException("O veículo da viagem deve ser informado (campo 'veiculoId').");
        }
        if (viagem.getMotorista() == null || viagem.getMotorista().getId() == null) {
            throw new NegocioException("O motorista da viagem deve ser informado (campo 'motoristaId').");
        }

        viagem.setVeiculo(veiculoServico.buscarVeiculoPorIdOuLancarErro(viagem.getVeiculo().getId()));
        viagem.setMotorista(motoristaServico.buscarMotoristaPorIdOuLancarErro(viagem.getMotorista().getId()));
    }

    private void validar(Viagem viagem) {
        if (!viagem.getVeiculo().isAtivo()) {
            throw new NegocioException(String.format(
                    "O veículo '%s' (id %d) está inativo e não pode ser usado em viagens. Ative-o ou escolha outro veículo.",
                    viagem.getVeiculo().getPlaca(), viagem.getVeiculo().getId()));
        }

        if (viagem.getStatus() != StatusViagemEnum.CANCELADA) {
            regrasAgenda.forEach(regra -> regra.validar(viagem));
        }
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new NegocioException(String.format(
                    "Período inválido: a data inicial (%s) é posterior à data final (%s).", inicio, fim));
        }
    }

    private Specification<Viagem> montarFiltro(StatusViagemEnum status, LocalDate inicio, LocalDate fim) {
        return (raiz, consulta, cb) -> {
            var predicados = new ArrayList<Predicate>();

            if (status != null) {
                predicados.add(cb.equal(raiz.get("status"), status));
            }
            if (inicio != null) {
                predicados.add(cb.greaterThanOrEqualTo(raiz.<LocalDateTime>get("dataHora"), inicio.atStartOfDay()));
            }
            if (fim != null) {
                predicados.add(cb.lessThanOrEqualTo(raiz.<LocalDateTime>get("dataHora"), fim.atTime(23, 59, 59)));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

}
