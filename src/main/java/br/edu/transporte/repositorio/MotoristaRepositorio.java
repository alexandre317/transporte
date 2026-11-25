package br.edu.transporte.repositorio;

import br.edu.transporte.modelo.Motorista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MotoristaRepositorio extends JpaRepository<Motorista, Long> {

    @Query("select count(m) from Motorista m where upper(m.cnh) = upper(:cnh)")
    long contarPorCnh(@Param("cnh") String cnh);

    @Query("select count(m) from Motorista m where upper(m.cnh) = upper(:cnh) and m.id <> :id")
    long contarPorCnhDeOutroMotorista(@Param("cnh") String cnh, @Param("id") Long id);

}
