package br.pucgoias.ads.hotel.repositorio;

import br.pucgoias.ads.hotel.dominio.Quarto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {
}
