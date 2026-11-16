package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.peca.Peca;
import br.com.oficina.domain.servico.Servico;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.veiculo.Veiculo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Raiz do agregado Ordem de Serviço.
 * Concentra as regras de transição de status, o cálculo do orçamento e o registro de aprovação/rejeição.
 * A baixa de estoque das peças é coordenada pela camada de aplicação, na mesma transação da aprovação.
 */
@Entity
@Table(name = "ordem_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código opaco (UUID) que o cliente usa para acompanhar a OS e aprovar o orçamento sem login. */
    @Column(name = "codigo_acompanhamento", nullable = false, unique = true, updatable = false, length = 36)
    private String codigoAcompanhamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOS status;

    @Enumerated(EnumType.STRING)
    @Column(name = "orcamento_status", nullable = false, length = 20)
    private OrcamentoStatus orcamentoStatus;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemServico> servicos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemPeca> pecas = new ArrayList<>();

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "inicio_execucao_em")
    private Instant inicioExecucaoEm;

    @Column(name = "fim_execucao_em")
    private Instant fimExecucaoEm;

    @Column(name = "entregue_em")
    private Instant entregueEm;

    protected OrdemServico() {
    }

    public static OrdemServico abrir(Cliente cliente, Veiculo veiculo) {
        if (cliente == null || veiculo == null) {
            throw new RegraNegocioException("Cliente e veículo são obrigatórios para abrir uma ordem de serviço");
        }
        OrdemServico os = new OrdemServico();
        os.cliente = cliente;
        os.veiculo = veiculo;
        os.status = StatusOS.RECEBIDA;
        os.orcamentoStatus = OrcamentoStatus.NAO_GERADO;
        os.codigoAcompanhamento = UUID.randomUUID().toString();
        os.criadaEm = Instant.now();
        return os;
    }

    // ---------- itens ----------

    public void adicionarServico(Servico servico) {
        exigirEdicaoPermitida();
        if (servico == null) {
            throw new RegraNegocioException("Serviço é obrigatório");
        }
        servicos.add(new ItemServico(servico));
    }

    public void adicionarPeca(Peca peca, int quantidade) {
        exigirEdicaoPermitida();
        if (peca == null) {
            throw new RegraNegocioException("Peça é obrigatória");
        }
        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade da peça deve ser maior que zero");
        }
        pecas.add(new ItemPeca(peca, quantidade));
    }

    private void exigirEdicaoPermitida() {
        if (status != StatusOS.RECEBIDA && status != StatusOS.EM_DIAGNOSTICO) {
            throw new RegraNegocioException("Itens só podem ser alterados nos status Recebida ou Em diagnóstico");
        }
    }

    // ---------- orçamento ----------

    public BigDecimal valorServicos() {
        return servicos.stream()
                .map(ItemServico::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal valorPecas() {
        return pecas.stream()
                .map(ItemPeca::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal valorTotal() {
        return valorServicos().add(valorPecas()).setScale(2, RoundingMode.HALF_UP);
    }

    // ---------- transições de status ----------

    public void iniciarDiagnostico() {
        transicionarPara(StatusOS.EM_DIAGNOSTICO);
    }

    /** Gera o orçamento a partir dos itens e o disponibiliza ao cliente para aprovação. */
    public void enviarOrcamento() {
        if (status == StatusOS.EM_DIAGNOSTICO && servicos.isEmpty() && pecas.isEmpty()) {
            throw new RegraNegocioException("Inclua ao menos um serviço ou peça antes de gerar o orçamento");
        }
        transicionarPara(StatusOS.AGUARDANDO_APROVACAO);
        orcamentoStatus = OrcamentoStatus.PENDENTE;
    }

    public void aprovarOrcamento() {
        aprovarOrcamento(Instant.now());
    }

    public void aprovarOrcamento(Instant agora) {
        exigirOrcamentoPendente();
        transicionarPara(StatusOS.EM_EXECUCAO);
        orcamentoStatus = OrcamentoStatus.APROVADO;
        inicioExecucaoEm = agora;
    }

    /** Rejeição encerra a OS sem execução; o veículo pode ser entregue ao cliente. */
    public void rejeitarOrcamento() {
        exigirOrcamentoPendente();
        transicionarPara(StatusOS.FINALIZADA);
        orcamentoStatus = OrcamentoStatus.REJEITADO;
    }

    public void finalizar() {
        finalizar(Instant.now());
    }

    public void finalizar(Instant agora) {
        if (status != StatusOS.EM_EXECUCAO) {
            throw new RegraNegocioException("Somente ordens em execução podem ser finalizadas");
        }
        transicionarPara(StatusOS.FINALIZADA);
        fimExecucaoEm = agora;
    }

    public void entregar() {
        transicionarPara(StatusOS.ENTREGUE);
        entregueEm = Instant.now();
    }

    private void exigirOrcamentoPendente() {
        if (status != StatusOS.AGUARDANDO_APROVACAO || orcamentoStatus != OrcamentoStatus.PENDENTE) {
            throw new RegraNegocioException("Não há orçamento pendente de aprovação para esta ordem de serviço");
        }
    }

    private void transicionarPara(StatusOS destino) {
        if (!status.podeTransicionarPara(destino)) {
            throw new RegraNegocioException("Transição inválida: de '" + status.getDescricao()
                    + "' para '" + destino.getDescricao() + "'");
        }
        status = destino;
    }

    /** Tempo efetivo de execução (aprovação até finalização), quando a OS passou por execução. */
    public Optional<Duration> duracaoExecucao() {
        if (inicioExecucaoEm == null || fimExecucaoEm == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(inicioExecucaoEm, fimExecucaoEm));
    }

    // ---------- getters ----------

    public Long getId() {
        return id;
    }

    public String getCodigoAcompanhamento() {
        return codigoAcompanhamento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public StatusOS getStatus() {
        return status;
    }

    public OrcamentoStatus getOrcamentoStatus() {
        return orcamentoStatus;
    }

    public List<ItemServico> getServicos() {
        return Collections.unmodifiableList(servicos);
    }

    public List<ItemPeca> getPecas() {
        return Collections.unmodifiableList(pecas);
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getInicioExecucaoEm() {
        return inicioExecucaoEm;
    }

    public Instant getFimExecucaoEm() {
        return fimExecucaoEm;
    }

    public Instant getEntregueEm() {
        return entregueEm;
    }
}
