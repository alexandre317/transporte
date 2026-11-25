package br.edu.transporte.configuracao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/** Escreve o mesmo formato de {@code MensagemErro} para erros gerados pelos filtros do Spring Security (401/403). */
final class EscritorErroJson {

    private EscritorErroJson() {
    }

    static void escrever(HttpServletRequest requisicao, HttpServletResponse resposta, int status,
                         String erro, String mensagem) throws IOException {
        var json = String.format(
                "{\"status\":%d,\"erro\":\"%s\",\"mensagem\":\"%s\",\"caminho\":\"%s\",\"momento\":\"%s\",\"detalhes\":[]}",
                status, escapar(erro), escapar(mensagem), escapar(requisicao.getRequestURI()), OffsetDateTime.now());

        resposta.setStatus(status);
        resposta.setContentType("application/json;charset=UTF-8");
        resposta.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String escapar(String texto) {
        var sb = new StringBuilder();
        for (var c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
