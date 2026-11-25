package br.edu.transporte.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PlacaJaCadastradaException extends ResponseStatusException {
    public PlacaJaCadastradaException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}
