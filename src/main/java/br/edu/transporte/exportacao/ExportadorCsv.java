package br.edu.transporte.exportacao;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ExportadorCsv implements Exportador {

    @Override
    public String formato() {
        return "csv";
    }

    @Override
    public MediaType tipoMidia() {
        return MediaType.parseMediaType("text/csv; charset=UTF-8");
    }

    @Override
    public byte[] gerar(String titulo, List<String> cabecalho, List<List<String>> linhas) {
        var sb = new StringBuilder("\uFEFF");
        sb.append(linha(cabecalho));
        for (var valores : linhas) {
            sb.append(linha(valores));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String linha(List<String> celulas) {
        var sb = new StringBuilder();
        for (var i = 0; i < celulas.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append('"').append(protegido(celulas.get(i)).replace("\"", "\"\"")).append('"');
        }
        return sb.append("\r\n").toString();
    }

    /** Evita injeção de fórmulas ao abrir o CSV em planilhas. */
    private String protegido(String valor) {
        if (valor == null) {
            return "";
        }
        if (!valor.isEmpty() && "=+-@\t\r".indexOf(valor.charAt(0)) >= 0) {
            return "'" + valor;
        }
        return valor;
    }
}
