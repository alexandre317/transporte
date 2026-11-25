package br.edu.transporte.dto.resposta;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class PainelResposta {

    private long totalVeiculos;

    private long totalMotoristas;

    private long totalViagens;

    private long viagensConcluidas;

    private long viagensPendentes;

    private BigDecimal faturamentoConcluido;

    /** Quantidade de viagens por mês do ano corrente (índice 0 = janeiro). */
    private List<Long> viagensPorMes;

    private List<AlertaManutencaoResposta> alertas;
}
