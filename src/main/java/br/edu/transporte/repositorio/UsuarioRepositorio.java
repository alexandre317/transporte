package br.edu.transporte.repositorio;

import br.edu.transporte.enums.PerfilEnum;
import br.edu.transporte.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    @Query("select u from Usuario u where lower(u.email) = lower(:email)")
    Optional<Usuario> buscarPorEmail(@Param("email") String email);

    @Query("select count(u) from Usuario u where lower(u.email) = lower(:email)")
    long contarPorEmail(@Param("email") String email);

    @Query("select count(u) from Usuario u where lower(u.email) = lower(:email) and u.id <> :id")
    long contarPorEmailDeOutroUsuario(@Param("email") String email, @Param("id") Long id);

    @Query("select count(u) from Usuario u where u.perfil = :perfil")
    long contarPorPerfil(@Param("perfil") PerfilEnum perfil);

}
