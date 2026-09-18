package br.pucgoias.ads.hotel.servico;

import br.pucgoias.ads.hotel.dominio.Hospede;
import br.pucgoias.ads.hotel.dominio.Periodo;
import br.pucgoias.ads.hotel.dominio.Quarto;
import br.pucgoias.ads.hotel.dominio.Reserva;
import br.pucgoias.ads.hotel.dominio.StatusReserva;
import br.pucgoias.ads.hotel.dto.OcupacaoQuarto;
import br.pucgoias.ads.hotel.excecao.QuartoIndisponivelException;
import br.pucgoias.ads.hotel.excecao.RecursoNaoEncontradoException;
import br.pucgoias.ads.hotel.repositorio.HospedeRepository;
import br.pucgoias.ads.hotel.repositorio.QuartoRepository;
import br.pucgoias.ads.hotel.repositorio.ReservaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservaService {

    private final HospedeRepository hospedeRepository;
    private final QuartoRepository quartoRepository;
    private final ReservaRepository reservaRepository;

    public ReservaService(HospedeRepository hospedeRepository,
                           QuartoRepository quartoRepository,
                           ReservaRepository reservaRepository) {
        this.hospedeRepository = hospedeRepository;
        this.quartoRepository = quartoRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional
    public Reserva reservar(Long hospedeId, Long quartoId, Periodo periodo) {
        Hospede hospede = hospedeRepository.findById(hospedeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Hospede", hospedeId));
        Quarto quarto = quartoRepository.findById(quartoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto", quartoId));

        boolean conflito = reservaRepository.existeConflito(
                quartoId, StatusReserva.ATIVA, periodo.getCheckIn(), periodo.getCheckOut());
        if (conflito) {
            throw new QuartoIndisponivelException(quarto.getNumero());
        }

        Reserva reserva = new Reserva(hospede, quarto, periodo, quarto.calcularValor(periodo));
        return reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelar(Long reservaId) {
        throw new UnsupportedOperationException();
    }

    public Page<Reserva> listarPorHospede(Long hospedeId, int pagina, int tamanho) {
        throw new UnsupportedOperationException();
    }

    public List<OcupacaoQuarto> relatorioOcupacao() {
        throw new UnsupportedOperationException();
    }

    public List<Reserva> listarAtivasComDetalhes() {
        throw new UnsupportedOperationException();
    }
}
