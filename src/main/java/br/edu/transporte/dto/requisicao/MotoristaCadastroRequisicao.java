package br.edu.transporte.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MotoristaCadastroRequisicao {

    @NotBlank(message = "O campo 'nome' é obrigatório.")
    @Size(max = 255, message = "O campo 'nome' deve ter no máximo 255 caracteres.")
    private String nome;

    @NotBlank(message = "O campo 'cnh' é obrigatório.")
    @Size(max = 20, message = "O campo 'cnh' deve ter no máximo 20 caracteres.")
    private String cnh;

    @Size(max = 255, message = "O campo 'telefone' deve ter no máximo 255 caracteres.")
    private String telefone;
}
