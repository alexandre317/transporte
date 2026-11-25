package br.edu.transporte.mapeador;

import br.edu.transporte.dto.requisicao.ManutencaoCadastroRequisicao;
import br.edu.transporte.dto.resposta.ManutencaoConsultaResposta;
import br.edu.transporte.dto.resposta.ManutencaoCadastroResposta;
import br.edu.transporte.modelo.Manutencao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ManutencaoMapeador {

    @Mapping(source = "veiculoId", target = "veiculo.id")
    @Mapping(target = "id", ignore = true)
    Manutencao paraManutencao(ManutencaoCadastroRequisicao requisicao);

    @Mapping(source = "veiculo.id", target = "veiculoId")
    @Mapping(source = "veiculo.placa", target = "veiculoPlaca")
    ManutencaoConsultaResposta paraConsultaResposta(Manutencao manutencao);

    List<ManutencaoConsultaResposta> paraListaConsultaResposta(List<Manutencao> manutencoes);

    @Mapping(source = "veiculo.id", target = "veiculoId")
    @Mapping(source = "veiculo.placa", target = "veiculoPlaca")
    ManutencaoCadastroResposta paraCadastroResposta(Manutencao manutencao);

}
