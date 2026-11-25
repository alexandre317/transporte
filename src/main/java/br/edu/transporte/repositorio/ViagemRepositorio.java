package br.edu.transporte.repositorio;

import br.edu.transporte.enums.StatusViagemEnum;
import br.edu.transporte.modelo.Viagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ViagemRepositorio extends JpaRepository<Viagem, Long>, JpaSpecificationExecutor<Viagem> {

    @Query("select coalesce(sum(v.valor), 0) from Viagem v where v.status = :status")
    BigDecimal somarValorPorStatus(@Param("status") StatusViagemEnum status);

    @Query("select count(v) from Viagem v where v.status = :status")
    long contarPorStatus(@Param("status") StatusViagemEnum status);

    @Query("select count(v) from Viagem v where v.veiculo.id = :veiculoId")
    long contarPorVeiculo(@Param("veiculoId") Long veiculoId);

    @Query("select count(v) from Viagem v where v.motorista.id = :motoristaId")
    long contarPorMotorista(@Param("motoristaId") Long motoristaId);

    @Query("select v from Viagem v where v.dataHora between :inicio and :fim order by v.dataHora")
    List<Viagem> listarPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("select count(v) from Viagem v where v.motorista.id = :motoristaId and v.id <> :idAtual "
            + "and v.status <> :cancelada and v.dataHora > :inicio and v.dataHora < :fim")
    long contarConflitosMotorista(@Param("motoristaId") Long motoristaId, @Param("idAtual") Long idAtual,
                                  @Param("cancelada") StatusViagemEnum cancelada,
                                  @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("select count(v) from Viagem v where v.veiculo.id = :veiculoId and v.id <> :idAtual "
            + "and v.status <> :cancelada and v.dataHora > :inicio and v.dataHora < :fim")
    long contarConflitosVeiculo(@Param("veiculoId") Long veiculoId, @Param("idAtual") Long idAtual,
                                @Param("cancelada") StatusViagemEnum cancelada,
                                @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

}
