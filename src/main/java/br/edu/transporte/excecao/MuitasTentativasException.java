package br.edu.transporte.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MuitasTentativasException extends ResponseStatusException {
    public MuitasTentativasException(String mensagem) {
        super(HttpStatus.TOO_MANY_REQUESTS, mensagem);
    }
}
