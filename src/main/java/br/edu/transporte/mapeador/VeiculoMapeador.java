package br.edu.transporte.mapeador;

import br.edu.transporte.dto.requisicao.VeiculoCadastroRequisicao;
import br.edu.transporte.dto.requisicao.VeiculoAtualizacaoRequisicao;
import br.edu.transporte.dto.resposta.VeiculoConsultaResposta;
import br.edu.transporte.dto.resposta.VeiculoCadastroResposta;
import br.edu.transporte.dto.resposta.VeiculoAtualizacaoResposta;
import br.edu.transporte.modelo.Veiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VeiculoMapeador {

    @Mapping(target = "id", ignore = true)
    Veiculo paraVeiculo(VeiculoCadastroRequisicao requisicao);

    @Mapping(target = "id", ignore = true)
    Veiculo paraVeiculo(VeiculoAtualizacaoRequisicao requisicao);

    VeiculoConsultaResposta paraConsultaResposta(Veiculo veiculo);

    List<VeiculoConsultaResposta> paraListaConsultaResposta(List<Veiculo> veiculos);

    VeiculoCadastroResposta paraCadastroResposta(Veiculo veiculo);

    VeiculoAtualizacaoResposta paraAtualizacaoResposta(Veiculo veiculo);

}
