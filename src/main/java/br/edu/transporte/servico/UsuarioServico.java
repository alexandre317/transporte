package br.edu.transporte.servico;

import br.edu.transporte.enums.PerfilEnum;
import br.edu.transporte.excecao.EmailJaCadastradoException;
import br.edu.transporte.excecao.NaoEncontradoException;
import br.edu.transporte.excecao.NegocioException;
import br.edu.transporte.modelo.Usuario;
import br.edu.transporte.repositorio.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder codificadorDeSenha;

    public List<Usuario> listar(Long id) {
        return id == null ? usuarioRepositorio.findAll() : Collections.singletonList(buscarUsuarioPorIdOuLancarErro(id));
    }

    public Usuario buscarUsuarioPorIdOuLancarErro(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new NaoEncontradoException(
                        String.format("Usuário com id %d não foi encontrado.", id)
                ));
    }

    public boolean existeAdministrador() {
        return usuarioRepositorio.contarPorPerfil(PerfilEnum.ADMIN) > 0;
    }

    /** Cria uma conta. Só é chamado por rotas exclusivas de administrador ou pela criação do primeiro admin. */
    @Transactional
    public Usuario cadastrar(Usuario usuarioParaSalvar) {
        usuarioParaSalvar.setNome(usuarioParaSalvar.getNome().trim());
        usuarioParaSalvar.setEmail(usuarioParaSalvar.getEmail().trim());

        if (usuarioParaSalvar.getPerfil() == null) {
            usuarioParaSalvar.setPerfil(PerfilEnum.USER);
        }

        garantirQueEmailNaoExiste(usuarioParaSalvar.getEmail());
        validarSenhaForte(usuarioParaSalvar.getSenha());

        usuarioParaSalvar.setSenha(codificadorDeSenha.encode(usuarioParaSalvar.getSenha()));

        return usuarioRepositorio.save(usuarioParaSalvar);
    }

    @Transactional
    public Usuario atualizarConta(Long id, Usuario dados, String senhaAtual, String novaSenha, String confirmarNovaSenha) {
        var existente = buscarUsuarioPorIdOuLancarErro(id);

        if (senhaAtual == null || senhaAtual.isBlank() || !codificadorDeSenha.matches(senhaAtual, existente.getSenha())) {
            throw new NegocioException("A senha atual informada está incorreta; nenhuma alteração foi feita.");
        }

        var email = dados.getEmail().trim();

        garantirQueEmailNaoExiste(email, id);

        existente.setNome(dados.getNome().trim());
        existente.setEmail(email);

        var informouNovaSenha = novaSenha != null && !novaSenha.isBlank();
        var informouConfirmacao = confirmarNovaSenha != null && !confirmarNovaSenha.isBlank();

        if (informouNovaSenha || informouConfirmacao) {
            if (!informouNovaSenha) {
                throw new NegocioException("Informe a nova senha no campo 'novaSenha' para confirmar a alteração.");
            }
            if (!informouConfirmacao) {
                throw new NegocioException("Repita a nova senha no campo 'confirmarNovaSenha'.");
            }
            if (!novaSenha.equals(confirmarNovaSenha)) {
                throw new NegocioException("A nova senha e a confirmação não são iguais.");
            }

            validarSenhaForte(novaSenha);

            existente.setSenha(codificadorDeSenha.encode(novaSenha));
        }

        return usuarioRepositorio.save(existente);
    }

    @Transactional
    public void excluir(Usuario usuario, Long idDoUsuarioAutenticado) {
        var usuarioParaExcluir = buscarUsuarioPorIdOuLancarErro(usuario.getId());

        if (usuarioParaExcluir.getId().equals(idDoUsuarioAutenticado)) {
            throw new NegocioException("Você não pode excluir a sua própria conta.");
        }

        if (usuarioParaExcluir.getPerfil() == PerfilEnum.ADMIN
                && usuarioRepositorio.contarPorPerfil(PerfilEnum.ADMIN) <= 1) {
            throw new NegocioException(
                    "Não é possível excluir o único administrador do sistema: o acesso administrativo seria perdido.");
        }

        usuarioRepositorio.delete(usuarioParaExcluir);
    }

    private void validarSenhaForte(String senha) {
        var problemas = new java.util.ArrayList<String>();

        if (senha == null || senha.length() < 10) {
            problemas.add("ao menos 10 caracteres");
        }
        if (senha == null || !senha.matches(".*[A-Z].*")) {
            problemas.add("uma letra maiúscula");
        }
        if (senha == null || !senha.matches(".*[a-z].*")) {
            problemas.add("uma letra minúscula");
        }
        if (senha == null || !senha.matches(".*\\d.*")) {
            problemas.add("um número");
        }
        if (senha == null || !senha.matches(".*[^A-Za-z0-9].*")) {
            problemas.add("um símbolo");
        }

        if (!problemas.isEmpty()) {
            throw new NegocioException("A senha não atende à política de segurança. Falta incluir: "
                    + String.join(", ", problemas) + ".");
        }
    }

    private void garantirQueEmailNaoExiste(String email) {
        if (usuarioRepositorio.contarPorEmail(email) > 0) {
            throw new EmailJaCadastradoException(
                    String.format("Já existe uma conta cadastrada com o e-mail/identificador '%s'.", email)
            );
        }
    }

    private void garantirQueEmailNaoExiste(String email, Long id) {
        if (usuarioRepositorio.contarPorEmailDeOutroUsuario(email, id) > 0) {
            throw new EmailJaCadastradoException(
                    String.format("O e-mail/identificador '%s' já está em uso por outra conta.", email)
            );
        }
    }

}
