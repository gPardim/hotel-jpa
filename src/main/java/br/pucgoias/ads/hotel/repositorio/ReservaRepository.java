package br.pucgoias.ads.hotel.repositorio;

import br.pucgoias.ads.hotel.dominio.Reserva;
import br.pucgoias.ads.hotel.dominio.StatusReserva;
import br.pucgoias.ads.hotel.dto.OcupacaoQuarto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existeConflito(Long quartoId, StatusReserva status, LocalDate checkIn, LocalDate checkOut);

    Page<Reserva> findByHospedeId(Long hospedeId, Pageable pageable);

    List<OcupacaoQuarto> relatorioOcupacao(StatusReserva status);

    List<Reserva> findByStatusOrderById(StatusReserva status);
}
