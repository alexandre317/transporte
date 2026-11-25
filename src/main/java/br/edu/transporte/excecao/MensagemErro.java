package br.edu.transporte.excecao;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Corpo padrão de todas as respostas de erro da API.
 *
 * @param status   código HTTP
 * @param erro     descrição curta do código HTTP
 * @param mensagem explicação do que aconteceu na operação
 * @param caminho  rota que foi chamada
 * @param momento  data/hora do erro
 * @param detalhes lista com detalhes adicionais (ex.: um item por campo inválido)
 */
public record MensagemErro(int status, String erro, String mensagem, String caminho,
                           OffsetDateTime momento, List<String> detalhes) {
}
