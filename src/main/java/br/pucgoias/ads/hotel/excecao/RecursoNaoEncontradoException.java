package br.pucgoias.ads.hotel.excecao;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super("%s nao encontrado para o identificador %d".formatted(recurso, id));
    }
}
