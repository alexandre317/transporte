package br.edu.transporte.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MinhaContaAtualizacaoRequisicao {

    @NotBlank(message = "O campo 'nome' é obrigatório.")
    @Size(max = 150, message = "O campo 'nome' deve ter no máximo 150 caracteres.")
    private String nome;

    @NotBlank(message = "O campo 'email' é obrigatório.")
    @Size(max = 255, message = "O campo 'email' deve ter no máximo 255 caracteres.")
    private String email;

    @NotBlank(message = "O campo 'senhaAtual' é obrigatório.")
    @Size(max = 72, message = "O campo 'senhaAtual' deve ter no máximo 72 caracteres.")
    private String senhaAtual;

    @Size(max = 72, message = "O campo 'novaSenha' deve ter no máximo 72 caracteres.")
    private String novaSenha;

    @Size(max = 72, message = "O campo 'confirmarNovaSenha' deve ter no máximo 72 caracteres.")
    private String confirmarNovaSenha;
}
