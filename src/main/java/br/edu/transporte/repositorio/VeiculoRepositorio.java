package br.edu.transporte.repositorio;

import br.edu.transporte.modelo.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VeiculoRepositorio extends JpaRepository<Veiculo, Long> {

    @Query("select count(v) from Veiculo v where upper(v.placa) = upper(:placa)")
    long contarPorPlaca(@Param("placa") String placa);

    @Query("select count(v) from Veiculo v where upper(v.placa) = upper(:placa) and v.id <> :id")
    long contarPorPlacaDeOutroVeiculo(@Param("placa") String placa, @Param("id") Long id);

}
