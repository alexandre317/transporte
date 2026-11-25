package br.edu.transporte.dto.resposta;

import br.edu.transporte.enums.PerfilEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioConsultaResposta {

    private Long id;

    private String nome;

    private String email;

    private PerfilEnum perfil;
}
