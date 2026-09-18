package br.pucgoias.ads.hotel.repositorio;

import br.pucgoias.ads.hotel.dominio.Hospede;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospedeRepository extends JpaRepository<Hospede, Long> {
}
