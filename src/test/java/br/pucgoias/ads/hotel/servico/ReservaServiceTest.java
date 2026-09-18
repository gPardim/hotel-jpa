package br.pucgoias.ads.hotel.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.pucgoias.ads.hotel.dominio.Hospede;
import br.pucgoias.ads.hotel.dominio.Periodo;
import br.pucgoias.ads.hotel.dominio.Quarto;
import br.pucgoias.ads.hotel.dominio.QuartoStandard;
import br.pucgoias.ads.hotel.dominio.QuartoSuite;
import br.pucgoias.ads.hotel.dominio.Reserva;
import br.pucgoias.ads.hotel.dominio.StatusReserva;
import br.pucgoias.ads.hotel.dto.OcupacaoQuarto;
import br.pucgoias.ads.hotel.excecao.PeriodoInvalidoException;
import br.pucgoias.ads.hotel.excecao.QuartoIndisponivelException;
import br.pucgoias.ads.hotel.repositorio.HospedeRepository;
import br.pucgoias.ads.hotel.repositorio.QuartoRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReservaServiceTest {

    @Autowired
    private ReservaService servico;

    @Autowired
    private HospedeRepository hospedeRepository;

    @Autowired
    private QuartoRepository quartoRepository;

    @Autowired
    private EntityManager entityManager;

    private Hospede novoHospede(String nome, String documento) {
        return hospedeRepository.save(new Hospede(nome, documento));
    }

    private Quarto novoStandard(String numero) {
        return quartoRepository.save(new QuartoStandard(numero, new BigDecimal("150.00")));
    }

    private Quarto novaSuite(String numero) {
        return quartoRepository.save(new QuartoSuite(numero, new BigDecimal("250.00"), new BigDecimal("40.00")));
    }

    @Test
    @DisplayName("1. Check-out nao posterior ao check-in lanca PeriodoInvalidoException")
    void checkoutInvalidoLancaExcecao() {
        assertThatThrownBy(() -> new Periodo(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 10)))
                .isInstanceOf(PeriodoInvalidoException.class);
    }

    @Test
    @DisplayName("2. Reserva sobreposta ATIVA no mesmo quarto lanca QuartoIndisponivelException")
    void reservaSobrepostaLancaExcecao() {
        Hospede hospede = novoHospede("Ana Souza", "11111111111");
        Quarto quarto = novoStandard("101");
        servico.reservar(hospede.getId(), quarto.getId(),
                new Periodo(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5)));

        assertThatThrownBy(() -> servico.reservar(hospede.getId(), quarto.getId(),
                new Periodo(LocalDate.of(2026, 2, 3), LocalDate.of(2026, 2, 8))))
                .isInstanceOf(QuartoIndisponivelException.class)
                .hasMessageContaining("101");
    }

    @Test
    @DisplayName("3. Reserva cancelada nao bloqueia sobreposicao no mesmo quarto")
    void reservaCanceladaNaoBloqueiaSobreposicao() {
        Hospede hospede = novoHospede("Bruno Lima", "22222222222");
        Quarto quarto = novoStandard("102");
        Reserva primeira = servico.reservar(hospede.getId(), quarto.getId(),
                new Periodo(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5)));

        servico.cancelar(primeira.getId());

        Reserva segunda = servico.reservar(hospede.getId(), quarto.getId(),
                new Periodo(LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 8)));

        assertThat(segunda.getId()).isNotNull();
    }

    @Test
    @DisplayName("4. Valor total de quarto Standard = diaria x noites")
    void valorTotalQuartoStandard() {
        Hospede hospede = novoHospede("Carla Dias", "33333333333");
        Quarto quarto = novoStandard("103");

        Reserva reserva = servico.reservar(hospede.getId(), quarto.getId(),
                new Periodo(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5)));

        assertThat(reserva.getValorTotal()).isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    @DisplayName("5. Valor total de Suite = diaria x noites + taxa de servico")
    void valorTotalQuartoSuite() {
        Hospede hospede = novoHospede("Diego Alves", "44444444444");
        Quarto quarto = novaSuite("201");

        Reserva reserva = servico.reservar(hospede.getId(), quarto.getId(),
                new Periodo(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 4)));

        assertThat(reserva.getValorTotal()).isEqualByComparingTo(new BigDecimal("790.00"));
    }

    @Test
    @DisplayName("6. Cancelamento altera o status sem chamada explicita a save")
    void cancelamentoAlteraStatusSemSaveExplicito() {
        Hospede hospede = novoHospede("Elisa Prado", "55555555555");
        Quarto quarto = novoStandard("104");
        Reserva reserva = servico.reservar(hospede.getId(), quarto.getId(),
                new Periodo(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5)));
        Long reservaId = reserva.getId();

        entityManager.flush();
        entityManager.clear();

        servico.cancelar(reservaId);

        entityManager.flush();
        entityManager.clear();

        Reserva recarregada = entityManager.find(Reserva.class, reservaId);

        assertThat(recarregada.getStatus()).isEqualTo(StatusReserva.CANCELADA);
    }

    @Test
    @DisplayName("7. Listagem de reservas do hospede e paginada e ordenada por check-in decrescente")
    void listagemPorHospedeEPaginadaEOrdenada() {
        Hospede hospede = novoHospede("Fabio Nunes", "66666666666");
        Hospede outroHospede = novoHospede("Gabriela Reis", "77777777777");
        Quarto quartoA = novoStandard("105");
        Quarto quartoB = novoStandard("106");
        Quarto quartoC = novoStandard("107");
        Quarto quartoD = novoStandard("108");

        servico.reservar(hospede.getId(), quartoA.getId(),
                new Periodo(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5)));
        servico.reservar(hospede.getId(), quartoB.getId(),
                new Periodo(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5)));
        servico.reservar(hospede.getId(), quartoC.getId(),
                new Periodo(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5)));
        servico.reservar(outroHospede.getId(), quartoD.getId(),
                new Periodo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5)));

        Page<Reserva> pagina = servico.listarPorHospede(hospede.getId(), 0, 2);

        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
        assertThat(pagina.getContent())
                .extracting(r -> r.getPeriodo().getCheckIn())
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("8. Relatorio de ocupacao retorna quantidade e receita por quarto")
    void relatorioDeOcupacaoRetornaQuantidadeEReceita() {
        Hospede hospede = novoHospede("Heitor Ramos", "88888888888");
        Quarto quarto = novoStandard("109");

        servico.reservar(hospede.getId(), quarto.getId(),
                new Periodo(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5)));

        List<OcupacaoQuarto> relatorio = servico.relatorioOcupacao();

        assertThat(relatorio).anySatisfy(item -> {
            assertThat(item.numeroQuarto()).isEqualTo("109");
            assertThat(item.quantidadeReservas()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("9. Listagem de reservas ativas carrega hospede e quarto em uma unica instrucao SQL")
    void listagemAtivasCarregaDetalhesEmUmaConsulta() {
        Hospede hospede = novoHospede("Igor Castro", "99999999999");
        Quarto quarto = novoStandard("110");
        servico.reservar(hospede.getId(), quarto.getId(),
                new Periodo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5)));

        entityManager.flush();
        entityManager.clear();

        Statistics estatisticas = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        List<Reserva> ativas = servico.listarAtivasComDetalhes();
        ativas.forEach(r -> {
            r.getHospede().getNome();
            r.getQuarto().getNumero();
        });

        assertThat(ativas).isNotEmpty();
        assertThat(estatisticas.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("10. Alteracao a partir de versao desatualizada e rejeitada (bloqueio otimista)")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void alteracaoComVersaoDesatualizadaERejeitada() {
        Quarto novo = quartoRepository.save(new QuartoStandard("301", new BigDecimal("180.00")));
        Long id = novo.getId();

        try {
            Quarto copiaA = quartoRepository.findById(id).orElseThrow();
            Quarto copiaB = quartoRepository.findById(id).orElseThrow();

            copiaA.reajustarDiaria(new BigDecimal("200.00"));
            quartoRepository.save(copiaA);

            copiaB.reajustarDiaria(new BigDecimal("210.00"));

            assertThatThrownBy(() -> quartoRepository.save(copiaB))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        } finally {
            quartoRepository.deleteById(id);
        }
    }
}
