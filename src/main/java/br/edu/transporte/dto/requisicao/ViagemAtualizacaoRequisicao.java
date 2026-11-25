package br.edu.transporte.dto.requisicao;

import br.edu.transporte.enums.StatusViagemEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViagemAtualizacaoRequisicao {

    @NotBlank(message = "O campo 'origem' é obrigatório.")
    @Size(max = 255, message = "O campo 'origem' deve ter no máximo 255 caracteres.")
    private String origem;

    @NotBlank(message = "O campo 'destino' é obrigatório.")
    @Size(max = 255, message = "O campo 'destino' deve ter no máximo 255 caracteres.")
    private String destino;

    @NotNull(message = "O campo 'dataHora' é obrigatório.")
    private LocalDateTime dataHora;

    @NotNull(message = "O campo 'valor' é obrigatório.")
    @DecimalMin(value = "0.0", message = "O valor não pode ser negativo.")
    private BigDecimal valor;

    @NotNull(message = "O campo 'status' é obrigatório.")
    private StatusViagemEnum status = StatusViagemEnum.AGENDADA;

    @NotNull(message = "O campo 'veiculoId' é obrigatório.")
    private Long veiculoId;

    @NotNull(message = "O campo 'motoristaId' é obrigatório.")
    private Long motoristaId;
}
