package br.edu.transporte.repositorio;

import br.edu.transporte.enums.SituacaoManutencaoEnum;
import br.edu.transporte.modelo.Manutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ManutencaoRepositorio extends JpaRepository<Manutencao, Long>, JpaSpecificationExecutor<Manutencao> {

    @Query("select m from Manutencao m order by m.dataManutencao desc")
    List<Manutencao> listarPorDataDecrescente();

    @Query("select count(m) from Manutencao m where m.veiculo.id = :veiculoId")
    long contarPorVeiculo(@Param("veiculoId") Long veiculoId);

    @Query("select count(m) from Manutencao m where m.veiculo.id = :veiculoId and m.situacao = :situacao")
    long contarPorVeiculoESituacao(@Param("veiculoId") Long veiculoId,
                                   @Param("situacao") SituacaoManutencaoEnum situacao);

    @Query("select count(m) from Manutencao m where m.veiculo.id = :veiculoId "
            + "and m.situacao = :situacao and m.dataManutencao = :data")
    long contarPorVeiculoSituacaoEData(@Param("veiculoId") Long veiculoId,
                                       @Param("situacao") SituacaoManutencaoEnum situacao,
                                       @Param("data") LocalDate data);

}
