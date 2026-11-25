package br.edu.transporte.mapeador;

import br.edu.transporte.dto.requisicao.ViagemCadastroRequisicao;
import br.edu.transporte.dto.requisicao.ViagemAtualizacaoRequisicao;
import br.edu.transporte.dto.resposta.ViagemConsultaResposta;
import br.edu.transporte.dto.resposta.ViagemCadastroResposta;
import br.edu.transporte.dto.resposta.ViagemAtualizacaoResposta;
import br.edu.transporte.modelo.Viagem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ViagemMapeador {

    @Mapping(source = "veiculoId", target = "veiculo.id")
    @Mapping(source = "motoristaId", target = "motorista.id")
    @Mapping(target = "id", ignore = true)
    Viagem paraViagem(ViagemCadastroRequisicao requisicao);

    @Mapping(source = "veiculoId", target = "veiculo.id")
    @Mapping(source = "motoristaId", target = "motorista.id")
    @Mapping(target = "id", ignore = true)
    Viagem paraViagem(ViagemAtualizacaoRequisicao requisicao);

    @Mapping(source = "veiculo.id", target = "veiculoId")
    @Mapping(source = "veiculo.placa", target = "veiculoPlaca")
    @Mapping(source = "motorista.id", target = "motoristaId")
    @Mapping(source = "motorista.nome", target = "motoristaNome")
    ViagemConsultaResposta paraConsultaResposta(Viagem viagem);

    List<ViagemConsultaResposta> paraListaConsultaResposta(List<Viagem> viagens);

    @Mapping(source = "veiculo.id", target = "veiculoId")
    @Mapping(source = "veiculo.placa", target = "veiculoPlaca")
    @Mapping(source = "motorista.id", target = "motoristaId")
    @Mapping(source = "motorista.nome", target = "motoristaNome")
    ViagemCadastroResposta paraCadastroResposta(Viagem viagem);

    @Mapping(source = "veiculo.id", target = "veiculoId")
    @Mapping(source = "veiculo.placa", target = "veiculoPlaca")
    @Mapping(source = "motorista.id", target = "motoristaId")
    @Mapping(source = "motorista.nome", target = "motoristaNome")
    ViagemAtualizacaoResposta paraAtualizacaoResposta(Viagem viagem);

}
