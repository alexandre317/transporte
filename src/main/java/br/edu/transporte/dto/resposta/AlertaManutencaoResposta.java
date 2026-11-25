package br.edu.transporte.dto.resposta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlertaManutencaoResposta {

    private String placa;

    private String modelo;

    private String motivo;

    private boolean vencida;
}
