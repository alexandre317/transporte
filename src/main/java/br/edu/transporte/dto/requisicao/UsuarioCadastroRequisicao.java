package br.edu.transporte.dto.requisicao;

import br.edu.transporte.enums.PerfilEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioCadastroRequisicao {

    @NotBlank(message = "O campo 'nome' é obrigatório.")
    @Size(max = 150, message = "O campo 'nome' deve ter no máximo 150 caracteres.")
    private String nome;

    @Email(message = "O e-mail informado é inválido.")
    @NotBlank(message = "O campo 'email' é obrigatório.")
    @Size(max = 255, message = "O campo 'email' deve ter no máximo 255 caracteres.")
    private String email;

    @NotBlank(message = "O campo 'senha' é obrigatório.")
    @Size(min = 10, max = 72, message = "A senha deve ter entre 10 e 72 caracteres.")
    private String senha;

    @NotNull(message = "O campo 'perfil' é obrigatório.")
    private PerfilEnum perfil = PerfilEnum.USER;
}
