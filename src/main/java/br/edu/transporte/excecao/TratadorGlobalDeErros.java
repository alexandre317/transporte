package br.edu.transporte.excecao;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Log4j2
@RestControllerAdvice
public class TratadorGlobalDeErros {

    private static final Pattern VALORES_ENUM =
            Pattern.compile("not one of the values accepted for Enum class: \\[(.*?)\\]");

    /** Cobre todas as exceções de negócio (404, 400, 401, 409, 429...). */
    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarResponseStatusException(ResponseStatusException e,
                                                                      HttpServletRequest requisicao) {
        var status = HttpStatus.valueOf(e.getStatusCode().value());
        var mensagem = e.getReason() != null ? e.getReason() : "Não foi possível concluir a operação.";

        if (status.is5xxServerError()) {
            log.error("erro de servidor em {} {}", requisicao.getMethod(), requisicao.getRequestURI(), e);
        }

        return resposta(status, mensagem, List.of(), requisicao);
    }

    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarValidacao(MethodArgumentNotValidException e,
                                                        HttpServletRequest requisicao) {
        var detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(f -> String.format("campo '%s': %s", f.getField(), f.getDefaultMessage()))
                .sorted()
                .toList();

        var mensagem = String.format("Falha de validação: %d campo(s) com problema. Corrija os dados e tente novamente.",
                detalhes.size());

        return resposta(HttpStatus.BAD_REQUEST, mensagem, detalhes, requisicao);
    }

    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarCorpoIlegivel(HttpMessageNotReadableException e,
                                                            HttpServletRequest requisicao) {
        var causa = e.getMostSpecificCause().getMessage();
        var detalhes = new java.util.ArrayList<String>();
        var mensagem = "O corpo da requisição está malformado ou contém valor inválido.";

        if (causa != null) {
            var enumeracao = VALORES_ENUM.matcher(causa);
            if (enumeracao.find()) {
                mensagem = "O corpo da requisição contém um valor inválido para um campo de opções.";
                detalhes.add("valores aceitos: " + enumeracao.group(1));
            } else if (causa.contains("could not be parsed") || causa.contains("Cannot deserialize value of type `java.time")) {
                mensagem = "O corpo da requisição contém data/hora em formato inválido.";
                detalhes.add("use o formato ISO-8601: datas 'aaaa-MM-dd' e data/hora 'aaaa-MM-ddTHH:mm:ss'");
            } else if (causa.contains("Cannot deserialize value of type")) {
                mensagem = "O corpo da requisição contém um valor com tipo incorreto (ex.: texto onde se esperava número).";
            } else if (causa.contains("Unexpected character") || causa.contains("Unrecognized token")
                    || causa.contains("end-of-input") || causa.contains("Required request body")) {
                mensagem = "O corpo da requisição não é um JSON válido ou está vazio.";
            }
        }

        return resposta(HttpStatus.BAD_REQUEST, mensagem, detalhes, requisicao);
    }

    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarTipoIncorreto(MethodArgumentTypeMismatchException e,
                                                            HttpServletRequest requisicao) {
        var tipo = e.getRequiredType();
        var detalhes = new java.util.ArrayList<String>();
        var esperado = tipo == null ? "valor válido" : tipo.getSimpleName();

        if (tipo != null && tipo.isEnum()) {
            detalhes.add("valores aceitos: " + Arrays.toString(tipo.getEnumConstants()));
        } else if (tipo != null && java.time.LocalDate.class.isAssignableFrom(tipo)) {
            detalhes.add("use o formato aaaa-MM-dd");
        }

        var mensagem = String.format("O parâmetro '%s' recebeu o valor '%s', que é inválido (esperado: %s).",
                e.getName(), e.getValue(), esperado);

        return resposta(HttpStatus.BAD_REQUEST, mensagem, detalhes, requisicao);
    }

    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarParametroAusente(MissingServletRequestParameterException e,
                                                               HttpServletRequest requisicao) {
        var mensagem = String.format("O parâmetro obrigatório '%s' (tipo %s) não foi informado.",
                e.getParameterName(), e.getParameterType());

        return resposta(HttpStatus.BAD_REQUEST, mensagem, List.of(), requisicao);
    }

    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarMetodoNaoPermitido(HttpRequestMethodNotSupportedException e,
                                                                 HttpServletRequest requisicao) {
        var suportados = e.getSupportedHttpMethods() == null ? "nenhum" : e.getSupportedHttpMethods().toString();
        var mensagem = String.format("O método %s não é permitido nesta rota. Métodos aceitos: %s.",
                e.getMethod(), suportados);

        return resposta(HttpStatus.METHOD_NOT_ALLOWED, mensagem, List.of(), requisicao);
    }

    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarTipoDeMidia(HttpMediaTypeNotSupportedException e,
                                                          HttpServletRequest requisicao) {
        var mensagem = String.format("O tipo de conteúdo '%s' não é suportado. Envie o corpo como application/json.",
                e.getContentType());

        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensagem, List.of(), requisicao);
    }

    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarRecursoNaoEncontrado(NoResourceFoundException e,
                                                                   HttpServletRequest requisicao) {
        var mensagem = String.format("A rota %s %s não existe.", requisicao.getMethod(), requisicao.getRequestURI());

        return resposta(HttpStatus.NOT_FOUND, mensagem, List.of(), requisicao);
    }

    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarIntegridadeDeDados(DataIntegrityViolationException e,
                                                                 HttpServletRequest requisicao) {
        log.warn("violação de integridade em {} {}: {}", requisicao.getMethod(), requisicao.getRequestURI(),
                e.getMostSpecificCause().getMessage());

        var causa = e.getMostSpecificCause().getMessage() == null ? "" : e.getMostSpecificCause().getMessage().toLowerCase();
        String mensagem;

        if (causa.contains("duplicate") || causa.contains("unique") || causa.contains("duplicad")) {
            mensagem = "Já existe um registro com os mesmos valores únicos (ex.: placa, CNH ou e-mail).";
        } else if (causa.contains("foreign key") || causa.contains("referenc")) {
            mensagem = "A operação viola um vínculo entre registros: o registro está em uso por outro "
                    + "(ou aponta para um registro inexistente).";
        } else if (causa.contains("null value") || causa.contains("not-null") || causa.contains("null")) {
            mensagem = "Um campo obrigatório ficou sem valor e o banco de dados rejeitou a operação.";
        } else if (causa.contains("too long") || causa.contains("value too long")) {
            mensagem = "Um dos textos informados excede o tamanho máximo permitido pelo banco de dados.";
        } else {
            mensagem = "O banco de dados rejeitou a operação por violação de integridade dos dados.";
        }

        return resposta(HttpStatus.CONFLICT, mensagem, List.of(), requisicao);
    }

    @ExceptionHandler
    public ResponseEntity<MensagemErro> tratarErroInesperado(Exception e, HttpServletRequest requisicao) {
        var codigo = UUID.randomUUID().toString();

        log.error("erro inesperado [{}] em {} {}", codigo, requisicao.getMethod(), requisicao.getRequestURI(), e);

        var mensagem = "Ocorreu um erro interno inesperado ao processar a operação. "
                + "Informe o código " + codigo + " ao administrador do sistema.";

        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, mensagem, List.of(), requisicao);
    }

    private ResponseEntity<MensagemErro> resposta(HttpStatus status, String mensagem, List<String> detalhes,
                                                  HttpServletRequest requisicao) {
        var corpo = new MensagemErro(status.value(), descricao(status), mensagem,
                requisicao.getRequestURI(), OffsetDateTime.now(), detalhes);

        return ResponseEntity.status(status).body(corpo);
    }

    private String descricao(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisição inválida";
            case UNAUTHORIZED -> "Não autenticado";
            case FORBIDDEN -> "Acesso negado";
            case NOT_FOUND -> "Não encontrado";
            case METHOD_NOT_ALLOWED -> "Método não permitido";
            case CONFLICT -> "Conflito";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de conteúdo não suportado";
            case TOO_MANY_REQUESTS -> "Muitas tentativas";
            case INTERNAL_SERVER_ERROR -> "Erro interno";
            default -> status.getReasonPhrase();
        };
    }

}
