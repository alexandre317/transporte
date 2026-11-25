package br.edu.transporte.servico;

import br.edu.transporte.dto.resposta.AlertaManutencaoResposta;
import br.edu.transporte.enums.SituacaoManutencaoEnum;
import br.edu.transporte.excecao.NaoEncontradoException;
import br.edu.transporte.excecao.NegocioException;
import br.edu.transporte.modelo.Manutencao;
import br.edu.transporte.repositorio.ManutencaoRepositorio;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManutencaoServico {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final ManutencaoRepositorio manutencaoRepositorio;
    private final VeiculoServico veiculoServico;

    @Value("${app.manutencao.alerta-dias}")
    private int diasDeAlerta;

    @Value("${app.manutencao.alerta-km}")
    private long kmDeAlerta;

    public Page<Manutencao> pesquisar(SituacaoManutencaoEnum situacao, LocalDate inicio, LocalDate fim, int pagina, int tamanho) {
        validarPeriodo(inicio, fim);

        var paginacao = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO_PAGINA),
                Sort.by(Sort.Direction.DESC, "dataManutencao"));

        return manutencaoRepositorio.findAll(montarFiltro(situacao, inicio, fim), paginacao);
    }

    public List<Manutencao> filtrar(SituacaoManutencaoEnum situacao, LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);

        return manutencaoRepositorio.findAll(montarFiltro(situacao, inicio, fim),
                Sort.by(Sort.Direction.DESC, "dataManutencao"));
    }

    public Manutencao buscarManutencaoPorIdOuLancarErro(Long id) {
        return manutencaoRepositorio.findById(id)
                .orElseThrow(() -> new NaoEncontradoException(
                        String.format("Manutenção com id %d não foi encontrada.", id)
                ));
    }

    @Transactional
    public Manutencao salvar(Manutencao manutencaoParaSalvar) {
        if (manutencaoParaSalvar.getVeiculo() == null || manutencaoParaSalvar.getVeiculo().getId() == null) {
            throw new NegocioException("O veículo da manutenção deve ser informado (campo 'veiculoId').");
        }

        var veiculo = veiculoServico.buscarVeiculoPorIdOuLancarErro(manutencaoParaSalvar.getVeiculo().getId());
        manutencaoParaSalvar.setVeiculo(veiculo);

        validar(manutencaoParaSalvar);

        var manutencaoSalva = manutencaoRepositorio.save(manutencaoParaSalvar);

        var quilometragem = manutencaoParaSalvar.getQuilometragem();
        if (veiculo.getQuilometragemAtual() == null || veiculo.getQuilometragemAtual() < quilometragem) {
            veiculoServico.atualizarQuilometragemAtual(veiculo, quilometragem);
        }

        return manutencaoSalva;
    }

    public List<AlertaManutencaoResposta> alertas() {
        var hoje = LocalDate.now();
        var formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var jaVistos = new HashSet<Long>();
        var alertas = new ArrayList<AlertaManutencaoResposta>();

        for (var m : manutencaoRepositorio.listarPorDataDecrescente()) {
            var v = m.getVeiculo();
            if (v == null || !v.isAtivo() || !jaVistos.add(v.getId())) {
                continue;
            }

            var motivos = new ArrayList<String>();
            var vencida = false;

            if (m.getProximaRevisao() != null) {
                var dias = ChronoUnit.DAYS.between(hoje, m.getProximaRevisao());
                if (dias < 0) {
                    vencida = true;
                    motivos.add("revisão vencida há " + (-dias) + " dia(s)");
                } else if (dias <= diasDeAlerta) {
                    motivos.add("revisão em " + dias + " dia(s) (" + m.getProximaRevisao().format(formato) + ")");
                }
            }

            if (m.getProximaRevisaoKm() != null && v.getQuilometragemAtual() != null) {
                var restante = m.getProximaRevisaoKm() - v.getQuilometragemAtual();
                if (restante <= 0) {
                    vencida = true;
                    motivos.add("ultrapassou em " + (-restante) + " km a revisão programada");
                } else if (restante <= kmDeAlerta) {
                    motivos.add("faltam " + restante + " km para a revisão");
                }
            }

            if (!motivos.isEmpty()) {
                alertas.add(new AlertaManutencaoResposta(v.getPlaca(), v.getModelo(), String.join(" · ", motivos), vencida));
            }
        }

        alertas.sort((a, b) -> Boolean.compare(b.isVencida(), a.isVencida()));

        return alertas;
    }

    private void validar(Manutencao manutencao) {
        if (manutencao.getProximaRevisao() != null
                && manutencao.getProximaRevisao().isBefore(manutencao.getDataManutencao())) {
            throw new NegocioException(String.format(
                    "A data da próxima revisão (%s) não pode ser anterior à data da manutenção (%s).",
                    manutencao.getProximaRevisao(), manutencao.getDataManutencao()));
        }

        if (manutencao.getProximaRevisaoKm() != null
                && manutencao.getProximaRevisaoKm() <= manutencao.getQuilometragem()) {
            throw new NegocioException(String.format(
                    "A quilometragem da próxima revisão (%d km) deve ser maior que a quilometragem da manutenção (%d km).",
                    manutencao.getProximaRevisaoKm(), manutencao.getQuilometragem()));
        }
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new NegocioException(String.format(
                    "Período inválido: a data inicial (%s) é posterior à data final (%s).", inicio, fim));
        }
    }

    private Specification<Manutencao> montarFiltro(SituacaoManutencaoEnum situacao, LocalDate inicio, LocalDate fim) {
        return (raiz, consulta, cb) -> {
            var predicados = new ArrayList<Predicate>();

            if (situacao != null) {
                predicados.add(cb.equal(raiz.get("situacao"), situacao));
            }
            if (inicio != null) {
                predicados.add(cb.greaterThanOrEqualTo(raiz.<LocalDate>get("dataManutencao"), inicio));
            }
            if (fim != null) {
                predicados.add(cb.lessThanOrEqualTo(raiz.<LocalDate>get("dataManutencao"), fim));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

}
