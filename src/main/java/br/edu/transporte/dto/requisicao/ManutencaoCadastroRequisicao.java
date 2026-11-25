package br.edu.transporte.dto.requisicao;

import br.edu.transporte.enums.SituacaoManutencaoEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManutencaoCadastroRequisicao {

    @NotNull(message = "O campo 'veiculoId' é obrigatório.")
    private Long veiculoId;

    @NotBlank(message = "O campo 'descricao' é obrigatório.")
    @Size(max = 255, message = "O campo 'descricao' deve ter no máximo 255 caracteres.")
    private String descricao;

    @NotNull(message = "O campo 'dataManutencao' é obrigatório.")
    private LocalDate dataManutencao;

    @NotNull(message = "O campo 'quilometragem' é obrigatório.")
    @Min(value = 0, message = "A quilometragem não pode ser negativa.")
    private Long quilometragem;

    private LocalDate proximaRevisao;

    @Min(value = 0, message = "A quilometragem da próxima revisão não pode ser negativa.")
    private Long proximaRevisaoKm;

    @NotNull(message = "O campo 'situacao' é obrigatório.")
    private SituacaoManutencaoEnum situacao = SituacaoManutencaoEnum.AGENDADA;
}
