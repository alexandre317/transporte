package br.edu.transporte.mapeador;

import br.edu.transporte.dto.requisicao.MinhaContaAtualizacaoRequisicao;
import br.edu.transporte.dto.requisicao.UsuarioCadastroRequisicao;
import br.edu.transporte.dto.resposta.UsuarioAtualizacaoResposta;
import br.edu.transporte.dto.resposta.UsuarioCadastroResposta;
import br.edu.transporte.dto.resposta.UsuarioConsultaResposta;
import br.edu.transporte.modelo.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapeador {

    UsuarioConsultaResposta paraConsultaResposta(Usuario usuario);

    List<UsuarioConsultaResposta> paraListaConsultaResposta(List<Usuario> usuarios);

    @Mapping(target = "id", ignore = true)
    Usuario paraUsuario(UsuarioCadastroRequisicao requisicao);

    UsuarioCadastroResposta paraCadastroResposta(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "perfil", ignore = true)
    Usuario paraUsuario(MinhaContaAtualizacaoRequisicao requisicao);

    UsuarioAtualizacaoResposta paraAtualizacaoResposta(Usuario usuario);

}
