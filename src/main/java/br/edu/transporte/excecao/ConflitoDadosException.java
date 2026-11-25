package br.edu.transporte.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ConflitoDadosException extends ResponseStatusException {
    public ConflitoDadosException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}
