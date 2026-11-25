package br.edu.transporte.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntrarRequisicao {

    @NotBlank(message = "O campo 'email' é obrigatório.")
    @Size(max = 255, message = "O campo 'email' deve ter no máximo 255 caracteres.")
    private String email;

    @NotBlank(message = "O campo 'senha' é obrigatório.")
    @Size(max = 72, message = "O campo 'senha' deve ter no máximo 72 caracteres.")
    private String senha;
}
