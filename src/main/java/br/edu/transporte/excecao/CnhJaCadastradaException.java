package br.edu.transporte.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CnhJaCadastradaException extends ResponseStatusException {
    public CnhJaCadastradaException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}
