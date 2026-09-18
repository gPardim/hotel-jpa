package br.pucgoias.ads.hotel.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "quarto")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", length = 20)
public abstract class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @Column(name = "valor_diaria", nullable = false)
    private BigDecimal valorDiaria;

    @Version
    private Long versao;

    protected Quarto() {
    }

    protected Quarto(String numero, BigDecimal valorDiaria) {
        this.numero = numero;
        this.valorDiaria = valorDiaria;
    }

    public abstract BigDecimal calcularValor(Periodo periodo);

    public void reajustarDiaria(BigDecimal novoValor) {
        this.valorDiaria = novoValor;
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public Long getVersao() {
        return versao;
    }
}
