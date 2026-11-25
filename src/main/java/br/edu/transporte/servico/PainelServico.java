package br.edu.transporte.servico;

import br.edu.transporte.dto.resposta.PainelResposta;
import br.edu.transporte.enums.StatusViagemEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PainelServico {

    private final VeiculoServico veiculoServico;
    private final MotoristaServico motoristaServico;
    private final ViagemServico viagemServico;
    private final ManutencaoServico manutencaoServico;

    public PainelResposta obterPainel() {
        return new PainelResposta(
                veiculoServico.contar(),
                motoristaServico.contar(),
                viagemServico.contar(),
                viagemServico.contarPorStatus(StatusViagemEnum.CONCLUIDA),
                viagemServico.contarPorStatus(StatusViagemEnum.AGENDADA),
                viagemServico.faturamentoConcluido(),
                viagemServico.viagensPorMes(),
                manutencaoServico.alertas());
    }

}
