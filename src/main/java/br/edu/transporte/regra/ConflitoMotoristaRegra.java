package br.edu.transporte.regra;

import br.edu.transporte.enums.StatusViagemEnum;
import br.edu.transporte.excecao.ConflitoAgendaException;
import br.edu.transporte.modelo.Viagem;
import br.edu.transporte.repositorio.ViagemRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ConflitoMotoristaRegra implements RegraAgenda {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ViagemRepositorio viagemRepositorio;

    @Override
    public void validar(Viagem viagem) {
        var idAtual = viagem.getId() == null ? -1L : viagem.getId();
        var inicio = viagem.getDataHora().minusHours(DURACAO_HORAS);
        var fim = viagem.getDataHora().plusHours(DURACAO_HORAS);

        var conflitos = viagemRepositorio.contarConflitosMotorista(viagem.getMotorista().getId(), idAtual, StatusViagemEnum.CANCELADA, inicio, fim);

        if (conflitos > 0) {
            throw new ConflitoAgendaException(String.format(
                    "Conflito de agenda: o motorista %s já possui %d outra(s) viagem(ns) entre %s e %s. "
                            + "Escolha um horário com pelo menos %dh de diferença ou altere o motorista.",
                    viagem.getMotorista().getNome(), conflitos, FORMATO.format(inicio), FORMATO.format(fim), DURACAO_HORAS));
        }
    }
}
