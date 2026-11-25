package br.edu.transporte.servico;

import br.edu.transporte.excecao.MuitasTentativasException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueia temporariamente novas tentativas de login depois de várias falhas seguidas
 * (proteção contra adivinhação de senha). O controle é em memória, por instância da aplicação.
 */
@Component
public class LimitadorTentativasLogin {

    private static final int MAXIMO_DE_FALHAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(15);
    private static final int LIMITE_DE_REGISTROS = 10_000;

    private record Registro(int falhas, Instant primeiraFalha) {
        boolean expirado() {
            return primeiraFalha.plus(JANELA).isBefore(Instant.now());
        }
    }

    private final ConcurrentHashMap<String, Registro> registros = new ConcurrentHashMap<>();

    public void verificarBloqueio(String chave) {
        var registro = registros.get(chave);

        if (registro == null) {
            return;
        }
        if (registro.expirado()) {
            registros.remove(chave);
            return;
        }
        if (registro.falhas() >= MAXIMO_DE_FALHAS) {
            var minutos = Math.max(1, Duration.between(Instant.now(), registro.primeiraFalha().plus(JANELA)).toMinutes() + 1);
            throw new MuitasTentativasException(String.format(
                    "Muitas tentativas de login malsucedidas (%d). Por segurança, tente novamente em cerca de %d minuto(s).",
                    registro.falhas(), minutos));
        }
    }

    public void registrarFalha(String chave) {
        if (registros.size() > LIMITE_DE_REGISTROS) {
            registros.entrySet().removeIf(entrada -> entrada.getValue().expirado());
        }

        registros.merge(chave, new Registro(1, Instant.now()),
                (atual, novo) -> atual.expirado() ? novo : new Registro(atual.falhas() + 1, atual.primeiraFalha()));
    }

    public void limpar(String chave) {
        registros.remove(chave);
    }
}
