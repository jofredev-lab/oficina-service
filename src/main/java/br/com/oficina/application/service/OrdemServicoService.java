package br.com.oficina.application.service;

import br.com.oficina.application.dto.AbrirOrdemServicoRequest;
import br.com.oficina.application.dto.AcompanhamentoResponse;
import br.com.oficina.application.dto.AdicionarItensRequest;
import br.com.oficina.application.dto.OrdemServicoResponse;
import br.com.oficina.application.dto.PecaItemRequest;
import br.com.oficina.application.dto.TempoMedioExecucaoResponse;
import br.com.oficina.application.dto.VeiculoOSRequest;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.ordemservico.ItemPeca;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.ordemservico.StatusOS;
import br.com.oficina.domain.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import br.com.oficina.infrastructure.persistence.ClienteRepository;
import br.com.oficina.infrastructure.persistence.OrdemServicoRepository;
import br.com.oficina.infrastructure.persistence.PecaRepository;
import br.com.oficina.infrastructure.persistence.ServicoRepository;
import br.com.oficina.infrastructure.persistence.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Casos de uso da Ordem de Serviço. Cada método público é uma transação: se qualquer regra falhar
 * (por exemplo, estoque insuficiente na aprovação), nada é persistido.
 */
@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordens;
    private final ClienteRepository clientes;
    private final VeiculoRepository veiculos;
    private final ServicoRepository servicos;
    private final PecaRepository pecas;

    public OrdemServicoService(OrdemServicoRepository ordens, ClienteRepository clientes,
                               VeiculoRepository veiculos, ServicoRepository servicos, PecaRepository pecas) {
        this.ordens = ordens;
        this.clientes = clientes;
        this.veiculos = veiculos;
        this.servicos = servicos;
        this.pecas = pecas;
    }

    // ---------- criação ----------

    @Transactional
    public OrdemServicoResponse abrir(AbrirOrdemServicoRequest r) {
        Documento documento = Documento.of(r.documentoCliente());
        Cliente cliente = clientes.findByDocumentoNumero(documento.getNumero())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente não encontrado para o CPF/CNPJ informado. Cadastre o cliente antes de abrir a OS."));
        Veiculo veiculo = obterOuCadastrarVeiculo(cliente, r.veiculo());

        OrdemServico os = OrdemServico.abrir(cliente, veiculo);
        incluirItens(os, r.servicosIds(), r.pecas());
        return OrdemServicoResponse.from(ordens.save(os));
    }

    private Veiculo obterOuCadastrarVeiculo(Cliente cliente, VeiculoOSRequest dados) {
        Placa placa = Placa.of(dados.placa());
        Optional<Veiculo> existente = veiculos.findByPlacaNumero(placa.getNumero());
        if (existente.isPresent()) {
            Veiculo veiculo = existente.get();
            if (!veiculo.getCliente().getId().equals(cliente.getId())) {
                throw new RegraNegocioException("O veículo de placa " + placa.getNumero()
                        + " está cadastrado para outro cliente");
            }
            return veiculo;
        }
        return veiculos.save(new Veiculo(placa, dados.marca(), dados.modelo(), dados.ano(), cliente));
    }

    @Transactional
    public OrdemServicoResponse adicionarItens(Long id, AdicionarItensRequest r) {
        OrdemServico os = obter(id);
        incluirItens(os, r.servicosIds(), r.pecas());
        return OrdemServicoResponse.from(os);
    }

    private void incluirItens(OrdemServico os, List<Long> servicosIds, List<PecaItemRequest> itensPeca) {
        if (servicosIds != null) {
            for (Long servicoId : servicosIds) {
                os.adicionarServico(servicos.findById(servicoId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado: " + servicoId)));
            }
        }
        if (itensPeca != null) {
            for (PecaItemRequest item : itensPeca) {
                os.adicionarPeca(pecas.findById(item.pecaId())
                                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada: " + item.pecaId())),
                        item.quantidade());
            }
        }
    }

    // ---------- consultas administrativas ----------

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listar(StatusOS status) {
        List<OrdemServico> lista = status == null
                ? ordens.findAllByOrderByCriadaEmAsc()
                : ordens.findByStatusOrderByCriadaEmAsc(status);
        return lista.stream().map(OrdemServicoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscar(Long id) {
        return OrdemServicoResponse.from(obter(id));
    }

    // ---------- fluxo de status ----------

    @Transactional
    public OrdemServicoResponse iniciarDiagnostico(Long id) {
        OrdemServico os = obter(id);
        os.iniciarDiagnostico();
        return OrdemServicoResponse.from(os);
    }

    @Transactional
    public OrdemServicoResponse enviarOrcamento(Long id) {
        OrdemServico os = obter(id);
        os.enviarOrcamento();
        return OrdemServicoResponse.from(os);
    }

    @Transactional
    public OrdemServicoResponse aprovarOrcamento(Long id) {
        OrdemServico os = obter(id);
        aplicarAprovacao(os);
        return OrdemServicoResponse.from(os);
    }

    @Transactional
    public OrdemServicoResponse rejeitarOrcamento(Long id) {
        OrdemServico os = obter(id);
        os.rejeitarOrcamento();
        return OrdemServicoResponse.from(os);
    }

    @Transactional
    public OrdemServicoResponse finalizar(Long id) {
        OrdemServico os = obter(id);
        os.finalizar();
        return OrdemServicoResponse.from(os);
    }

    @Transactional
    public OrdemServicoResponse entregar(Long id) {
        OrdemServico os = obter(id);
        os.entregar();
        return OrdemServicoResponse.from(os);
    }

    /** Aprova o orçamento e baixa o estoque das peças na mesma transação (tudo ou nada). */
    private void aplicarAprovacao(OrdemServico os) {
        os.aprovarOrcamento();
        for (ItemPeca item : os.getPecas()) {
            item.getPeca().baixarEstoque(item.getQuantidade());
        }
    }

    // ---------- acompanhamento pelo cliente ----------

    @Transactional(readOnly = true)
    public AcompanhamentoResponse consultarAcompanhamento(String codigo) {
        return AcompanhamentoResponse.from(obterPorCodigo(codigo));
    }

    @Transactional
    public AcompanhamentoResponse aprovarPorCodigo(String codigo) {
        OrdemServico os = obterPorCodigo(codigo);
        aplicarAprovacao(os);
        return AcompanhamentoResponse.from(os);
    }

    @Transactional
    public AcompanhamentoResponse rejeitarPorCodigo(String codigo) {
        OrdemServico os = obterPorCodigo(codigo);
        os.rejeitarOrcamento();
        return AcompanhamentoResponse.from(os);
    }

    // ---------- métricas ----------

    @Transactional(readOnly = true)
    public TempoMedioExecucaoResponse tempoMedioExecucao() {
        List<Duration> duracoes = ordens.findByInicioExecucaoEmIsNotNullAndFimExecucaoEmIsNotNull().stream()
                .map(OrdemServico::duracaoExecucao)
                .flatMap(Optional::stream)
                .toList();
        if (duracoes.isEmpty()) {
            return new TempoMedioExecucaoResponse(0, 0.0, "sem dados");
        }
        double mediaSegundos = duracoes.stream().mapToLong(Duration::getSeconds).average().orElse(0);
        long total = Math.round(mediaSegundos);
        String formatado = (total / 3600) + "h " + ((total % 3600) / 60) + "min";
        return new TempoMedioExecucaoResponse(duracoes.size(), mediaSegundos / 60.0, formatado);
    }

    // ---------- helpers ----------

    private OrdemServico obter(Long id) {
        return ordens.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada: " + id));
    }

    private OrdemServico obterPorCodigo(String codigo) {
        return ordens.findByCodigoAcompanhamento(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada para o código informado"));
    }
}
