package br.edu.transporte.dto.resposta;

import br.edu.transporte.enums.SituacaoManutencaoEnum;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManutencaoCadastroResposta {

    private Long id;

    private Long veiculoId;

    private String veiculoPlaca;

    private String descricao;

    private LocalDate dataManutencao;

    private Long quilometragem;

    private LocalDate proximaRevisao;

    private Long proximaRevisaoKm;

    private SituacaoManutencaoEnum situacao;
}
