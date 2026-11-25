package br.edu.transporte.exportacao;

import org.springframework.http.MediaType;

import java.util.List;

public interface Exportador {

    String formato();

    MediaType tipoMidia();

    byte[] gerar(String titulo, List<String> cabecalho, List<List<String>> linhas);
}
