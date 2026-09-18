package br.pucgoias.ads.hotel.excecao;

public class QuartoIndisponivelException extends RuntimeException {

    public QuartoIndisponivelException(String numeroQuarto) {
        super("Quarto " + numeroQuarto + " indisponivel para o periodo informado");
    }
}
