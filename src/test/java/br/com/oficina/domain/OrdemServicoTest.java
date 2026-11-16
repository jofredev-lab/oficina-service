package br.com.oficina.domain;

import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.ordemservico.OrcamentoStatus;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.ordemservico.StatusOS;
import br.com.oficina.domain.peca.Peca;
import br.com.oficina.domain.servico.Servico;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdemServicoTest {

    private Cliente cliente;
    private Veiculo veiculo;
    private Servico trocaOleo;
    private Peca filtro;
    private OrdemServico os;

    @BeforeEach
    void preparar() {
        cliente = new Cliente("João", Documento.of("529.982.247-25"), "joao@email.com", "61999999999");
        veiculo = new Veiculo(Placa.of("ABC1D23"), "Fiat", "Uno", 2015, cliente);
        trocaOleo = new Servico("Troca de óleo", "Óleo 5W30", new BigDecimal("80.00"));
        filtro = new Peca("FO-1", "Filtro de óleo", new BigDecimal("25.50"), 10, 2);
        os = OrdemServico.abrir(cliente, veiculo);
    }

    @Test
    void novaOsNasceRecebidaSemOrcamentoEComCodigoDeAcompanhamento() {
        assertEquals(StatusOS.RECEBIDA, os.getStatus());
        assertEquals(OrcamentoStatus.NAO_GERADO, os.getOrcamentoStatus());
        assertNotNull(os.getCodigoAcompanhamento());
        assertNotNull(os.getCriadaEm());
        assertEquals(cliente, os.getCliente());
        assertEquals(veiculo, os.getVeiculo());
    }

    @Test
    void exigeClienteEVeiculo() {
        assertThrows(RegraNegocioException.class, () -> OrdemServico.abrir(null, veiculo));
        assertThrows(RegraNegocioException.class, () -> OrdemServico.abrir(cliente, null));
    }

    @Test
    void calculaOrcamentoComServicosEPecas() {
        os.adicionarServico(trocaOleo);
        os.adicionarPeca(filtro, 2);
        assertEquals(new BigDecimal("80.00"), os.valorServicos());
        assertEquals(new BigDecimal("51.00"), os.valorPecas());
        assertEquals(new BigDecimal("131.00"), os.valorTotal());
        assertEquals(1, os.getServicos().size());
        assertEquals(1, os.getPecas().size());
        assertEquals(new BigDecimal("51.00"), os.getPecas().get(0).subtotal());
        assertEquals("Filtro de óleo", os.getPecas().get(0).getNomePeca());
        assertEquals("Troca de óleo", os.getServicos().get(0).getNomeServico());
    }

    @Test
    void validaItensAdicionados() {
        assertThrows(RegraNegocioException.class, () -> os.adicionarServico(null));
        assertThrows(RegraNegocioException.class, () -> os.adicionarPeca(null, 1));
        assertThrows(RegraNegocioException.class, () -> os.adicionarPeca(filtro, 0));
    }

    @Test
    void fluxoCompletoAteEntrega() {
        os.adicionarServico(trocaOleo);
        os.adicionarPeca(filtro, 2);

        os.iniciarDiagnostico();
        assertEquals(StatusOS.EM_DIAGNOSTICO, os.getStatus());

        os.enviarOrcamento();
        assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.getStatus());
        assertEquals(OrcamentoStatus.PENDENTE, os.getOrcamentoStatus());

        Instant inicio = Instant.parse("2026-01-10T10:00:00Z");
        os.aprovarOrcamento(inicio);
        assertEquals(StatusOS.EM_EXECUCAO, os.getStatus());
        assertEquals(OrcamentoStatus.APROVADO, os.getOrcamentoStatus());
        assertEquals(inicio, os.getInicioExecucaoEm());
        assertEquals(Optional.empty(), os.duracaoExecucao());

        os.finalizar(inicio.plus(Duration.ofMinutes(90)));
        assertEquals(StatusOS.FINALIZADA, os.getStatus());
        assertEquals(Optional.of(Duration.ofMinutes(90)), os.duracaoExecucao());

        os.entregar();
        assertEquals(StatusOS.ENTREGUE, os.getStatus());
        assertNotNull(os.getEntregueEm());
    }

    @Test
    void aprovacaoSemInstanteExplicitoRegistraInicioDaExecucao() {
        os.adicionarServico(trocaOleo);
        os.iniciarDiagnostico();
        os.enviarOrcamento();
        os.aprovarOrcamento();
        assertNotNull(os.getInicioExecucaoEm());
        os.finalizar();
        assertNotNull(os.getFimExecucaoEm());
        assertTrue(os.duracaoExecucao().isPresent());
    }

    @Test
    void rejeicaoFinalizaSemExecucaoENaoGeraDuracao() {
        os.adicionarServico(trocaOleo);
        os.iniciarDiagnostico();
        os.enviarOrcamento();
        os.rejeitarOrcamento();
        assertEquals(StatusOS.FINALIZADA, os.getStatus());
        assertEquals(OrcamentoStatus.REJEITADO, os.getOrcamentoStatus());
        assertEquals(Optional.empty(), os.duracaoExecucao());
        os.entregar();
        assertEquals(StatusOS.ENTREGUE, os.getStatus());
    }

    @Test
    void naoGeraOrcamentoSemItens() {
        os.iniciarDiagnostico();
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> os.enviarOrcamento());
        assertTrue(ex.getMessage().contains("ao menos um serviço ou peça"));
        assertEquals(StatusOS.EM_DIAGNOSTICO, os.getStatus());
    }

    @Test
    void naoPermitePularEtapas() {
        os.adicionarServico(trocaOleo);
        assertThrows(RegraNegocioException.class, () -> os.enviarOrcamento());
        assertThrows(RegraNegocioException.class, () -> os.aprovarOrcamento());
        assertThrows(RegraNegocioException.class, () -> os.rejeitarOrcamento());
        assertThrows(RegraNegocioException.class, () -> os.finalizar());
        assertThrows(RegraNegocioException.class, () -> os.entregar());
        os.iniciarDiagnostico();
        assertThrows(RegraNegocioException.class, () -> os.iniciarDiagnostico());
        assertThrows(RegraNegocioException.class, () -> os.aprovarOrcamento());
        os.enviarOrcamento();
        assertThrows(RegraNegocioException.class, () -> os.finalizar());
        assertThrows(RegraNegocioException.class, () -> os.entregar());
    }

    @Test
    void naoPermiteAlterarItensDepoisDoEnvioDoOrcamento() {
        os.adicionarServico(trocaOleo);
        os.iniciarDiagnostico();
        os.enviarOrcamento();
        assertThrows(RegraNegocioException.class, () -> os.adicionarServico(trocaOleo));
        assertThrows(RegraNegocioException.class, () -> os.adicionarPeca(filtro, 1));
    }

    @Test
    void naoPermiteAprovarDuasVezes() {
        os.adicionarServico(trocaOleo);
        os.iniciarDiagnostico();
        os.enviarOrcamento();
        os.aprovarOrcamento();
        assertThrows(RegraNegocioException.class, () -> os.aprovarOrcamento());
        assertThrows(RegraNegocioException.class, () -> os.rejeitarOrcamento());
    }

    @Test
    void regrasDeTransicaoDoStatus() {
        assertEquals(1, StatusOS.RECEBIDA.proximosPermitidos().size());
        assertTrue(StatusOS.AGUARDANDO_APROVACAO.podeTransicionarPara(StatusOS.EM_EXECUCAO));
        assertTrue(StatusOS.AGUARDANDO_APROVACAO.podeTransicionarPara(StatusOS.FINALIZADA));
        assertTrue(StatusOS.ENTREGUE.proximosPermitidos().isEmpty());
        assertEquals("Em diagnóstico", StatusOS.EM_DIAGNOSTICO.getDescricao());
        for (StatusOS status : StatusOS.values()) {
            assertTrue(!status.podeTransicionarPara(status), "status não pode transicionar para si mesmo: " + status);
        }
    }
}
