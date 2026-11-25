package br.edu.transporte.dto.resposta;

import br.edu.transporte.enums.TipoVeiculoEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VeiculoCadastroResposta {

    private Long id;

    private String placa;

    private String modelo;

    private int capacidade;

    private TipoVeiculoEnum tipo;

    private boolean ativo;

    private Long quilometragemAtual;
}
