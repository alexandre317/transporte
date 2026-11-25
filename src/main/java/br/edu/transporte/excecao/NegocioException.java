package br.edu.transporte.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NegocioException extends ResponseStatusException {
    public NegocioException(String mensagem) {
        super(HttpStatus.BAD_REQUEST, mensagem);
    }
}
