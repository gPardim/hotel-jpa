package br.pucgoias.ads.hotel.dominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("STANDARD")
public class QuartoStandard extends Quarto {

    protected QuartoStandard() {
    }

    public QuartoStandard(String numero, BigDecimal valorDiaria) {
        super(numero, valorDiaria);
    }

    @Override
    public BigDecimal calcularValor(Periodo periodo) {
        return getValorDiaria().multiply(BigDecimal.valueOf(periodo.noites()));
    }
}
