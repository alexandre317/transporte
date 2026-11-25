package br.edu.transporte.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmailJaCadastradoException extends ResponseStatusException {
    public EmailJaCadastradoException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}
