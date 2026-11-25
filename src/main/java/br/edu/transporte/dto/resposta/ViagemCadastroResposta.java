package br.edu.transporte.dto.resposta;

import br.edu.transporte.enums.StatusViagemEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViagemCadastroResposta {

    private Long id;

    private String origem;

    private String destino;

    private LocalDateTime dataHora;

    private BigDecimal valor;

    private StatusViagemEnum status;

    private Long veiculoId;

    private String veiculoPlaca;

    private Long motoristaId;

    private String motoristaNome;
}
