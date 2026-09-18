package br.pucgoias.ads.hotel.dto;

import java.math.BigDecimal;

public record OcupacaoQuarto(String numeroQuarto, Long quantidadeReservas, BigDecimal receita) {
}
