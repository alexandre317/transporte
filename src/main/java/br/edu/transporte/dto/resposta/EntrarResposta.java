package br.edu.transporte.dto.resposta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EntrarResposta {

    private String token;

    private Long usuarioId;

    private String nome;

    private String perfil;
}
