package br.pucgoias.ads.hotel.repositorio;

import br.pucgoias.ads.hotel.dominio.Reserva;
import br.pucgoias.ads.hotel.dominio.StatusReserva;
import br.pucgoias.ads.hotel.dto.OcupacaoQuarto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
            select case when count(r) > 0 then true else false end
            from Reserva r
            where r.quarto.id = :quartoId
              and r.status = :status
              and r.periodo.checkIn < :checkOut
              and :checkIn < r.periodo.checkOut
            """)
    boolean existeConflito(@Param("quartoId") Long quartoId,
                            @Param("status") StatusReserva status,
                            @Param("checkIn") LocalDate checkIn,
                            @Param("checkOut") LocalDate checkOut);

    Page<Reserva> findByHospedeId(Long hospedeId, Pageable pageable);

    @Query("""
            select new br.pucgoias.ads.hotel.dto.OcupacaoQuarto(q.numero, count(r), sum(r.valorTotal))
            from Reserva r join r.quarto q
            where r.status = :status
            group by q.numero
            order by q.numero
            """)
    List<OcupacaoQuarto> relatorioOcupacao(@Param("status") StatusReserva status);

    @EntityGraph(attributePaths = {"hospede", "quarto"})
    List<Reserva> findByStatusOrderById(StatusReserva status);
}
