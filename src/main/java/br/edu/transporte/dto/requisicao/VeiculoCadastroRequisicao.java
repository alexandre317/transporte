package br.edu.transporte.dto.requisicao;

import br.edu.transporte.enums.TipoVeiculoEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VeiculoCadastroRequisicao {

    @NotBlank(message = "O campo 'placa' é obrigatório.")
    @Size(max = 10, message = "O campo 'placa' deve ter no máximo 10 caracteres.")
    private String placa;

    @NotBlank(message = "O campo 'modelo' é obrigatório.")
    @Size(max = 255, message = "O campo 'modelo' deve ter no máximo 255 caracteres.")
    private String modelo;

    @NotNull(message = "O campo 'capacidade' é obrigatório.")
    @Min(value = 1, message = "A capacidade deve ser maior que zero.")
    private Integer capacidade;

    @NotNull(message = "O campo 'tipo' é obrigatório.")
    private TipoVeiculoEnum tipo;

    private Boolean ativo = true;

    @Min(value = 0, message = "A quilometragem atual não pode ser negativa.")
    private Long quilometragemAtual = 0L;
}
