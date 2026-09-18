package br.pucgoias.ads.hotel.dominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("SUITE")
public class QuartoSuite extends Quarto {

    private BigDecimal taxaServico;

    protected QuartoSuite() {
    }

    public QuartoSuite(String numero, BigDecimal valorDiaria, BigDecimal taxaServico) {
        super(numero, valorDiaria);
        this.taxaServico = taxaServico;
    }

    @Override
    public BigDecimal calcularValor(Periodo periodo) {
        return getValorDiaria().multiply(BigDecimal.valueOf(periodo.noites())).add(taxaServico);
    }

    public BigDecimal getTaxaServico() {
        return taxaServico;
    }
}
