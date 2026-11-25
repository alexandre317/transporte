package br.edu.transporte.mapeador;

import br.edu.transporte.dto.requisicao.MotoristaCadastroRequisicao;
import br.edu.transporte.dto.requisicao.MotoristaAtualizacaoRequisicao;
import br.edu.transporte.dto.resposta.MotoristaConsultaResposta;
import br.edu.transporte.dto.resposta.MotoristaCadastroResposta;
import br.edu.transporte.dto.resposta.MotoristaAtualizacaoResposta;
import br.edu.transporte.modelo.Motorista;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MotoristaMapeador {

    @Mapping(target = "id", ignore = true)
    Motorista paraMotorista(MotoristaCadastroRequisicao requisicao);

    @Mapping(target = "id", ignore = true)
    Motorista paraMotorista(MotoristaAtualizacaoRequisicao requisicao);

    MotoristaConsultaResposta paraConsultaResposta(Motorista motorista);

    List<MotoristaConsultaResposta> paraListaConsultaResposta(List<Motorista> motoristas);

    MotoristaCadastroResposta paraCadastroResposta(Motorista motorista);

    MotoristaAtualizacaoResposta paraAtualizacaoResposta(Motorista motorista);

}
