package br.edu.transporte.regra;

import br.edu.transporte.enums.SituacaoManutencaoEnum;
import br.edu.transporte.enums.StatusViagemEnum;
import br.edu.transporte.excecao.ConflitoAgendaException;
import br.edu.transporte.modelo.Viagem;
import br.edu.transporte.repositorio.ManutencaoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VeiculoEmManutencaoRegra implements RegraAgenda {

    private final ManutencaoRepositorio manutencaoRepositorio;

    @Override
    public void validar(Viagem viagem) {
        if (viagem.getStatus() == StatusViagemEnum.CONCLUIDA) {
            return;
        }

        var veiculoId = viagem.getVeiculo().getId();
        var dia = viagem.getDataHora().toLocalDate();

        if (manutencaoRepositorio.contarPorVeiculoESituacao(veiculoId, SituacaoManutencaoEnum.EM_ANDAMENTO) > 0) {
            throw new ConflitoAgendaException(String.format(
                    "O veículo '%s' está com uma manutenção em andamento e não pode receber viagens agora.",
                    viagem.getVeiculo().getPlaca()));
        }

        if (manutencaoRepositorio.contarPorVeiculoSituacaoEData(veiculoId, SituacaoManutencaoEnum.AGENDADA, dia) > 0) {
            throw new ConflitoAgendaException(String.format(
                    "O veículo '%s' tem manutenção agendada para %s, mesmo dia da viagem. Escolha outra data ou outro veículo.",
                    viagem.getVeiculo().getPlaca(), dia));
        }
    }
}
