package br.com.oficina.application.dto;

import br.com.oficina.domain.ordemservico.OrdemServico;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrdemServicoResponse(
        Long id,
        String codigoAcompanhamento,
        Long clienteId,
        String clienteNome,
        String clienteDocumento,
        String veiculoPlaca,
        String veiculoMarca,
        String veiculoModelo,
        Integer veiculoAno,
        String status,
        String statusDescricao,
        String orcamentoStatus,
        List<ItemServicoResponse> servicos,
        List<ItemPecaResponse> pecas,
        BigDecimal valorServicos,
        BigDecimal valorPecas,
        BigDecimal valorTotal,
        Instant criadaEm,
        Instant inicioExecucaoEm,
        Instant fimExecucaoEm,
        Instant entregueEm) {

    public static OrdemServicoResponse from(OrdemServico os) {
        return new OrdemServicoResponse(
                os.getId(),
                os.getCodigoAcompanhamento(),
                os.getCliente().getId(),
                os.getCliente().getNome(),
                os.getCliente().getDocumento().getNumero(),
                os.getVeiculo().getPlaca().getNumero(),
                os.getVeiculo().getMarca(),
                os.getVeiculo().getModelo(),
                os.getVeiculo().getAno(),
                os.getStatus().name(),
                os.getStatus().getDescricao(),
                os.getOrcamentoStatus().name(),
                os.getServicos().stream().map(ItemServicoResponse::from).toList(),
                os.getPecas().stream().map(ItemPecaResponse::from).toList(),
                os.valorServicos(),
                os.valorPecas(),
                os.valorTotal(),
                os.getCriadaEm(),
                os.getInicioExecucaoEm(),
                os.getFimExecucaoEm(),
                os.getEntregueEm());
    }
}
