package br.pucgoias.ads.hotel.dominio;

import br.pucgoias.ads.hotel.excecao.PeriodoInvalidoException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Embeddable
public class Periodo {

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    protected Periodo() {
    }

    public Periodo(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new PeriodoInvalidoException("check-out deve ser posterior ao check-in");
        }
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public long noites() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public boolean sobrepoe(Periodo outro) {
        return checkIn.isBefore(outro.checkOut) && outro.checkIn.isBefore(checkOut);
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Periodo)) return false;
        Periodo periodo = (Periodo) o;
        return Objects.equals(checkIn, periodo.checkIn) && Objects.equals(checkOut, periodo.checkOut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkIn, checkOut);
    }
}
