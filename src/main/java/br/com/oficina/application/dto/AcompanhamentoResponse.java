package br.com.oficina.application.dto;

import br.com.oficina.domain.ordemservico.OrdemServico;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Visão pública da OS para o cliente (não expõe dados pessoais). */
public record AcompanhamentoResponse(
        String codigoAcompanhamento,
        String status,
        String statusDescricao,
        String veiculoPlaca,
        String veiculoModelo,
        String orcamentoStatus,
        List<ItemServicoResponse> servicos,
        List<ItemPecaResponse> pecas,
        BigDecimal valorTotal,
        Instant criadaEm,
        Instant inicioExecucaoEm,
        Instant fimExecucaoEm,
        Instant entregueEm) {

    public static AcompanhamentoResponse from(OrdemServico os) {
        return new AcompanhamentoResponse(
                os.getCodigoAcompanhamento(),
                os.getStatus().name(),
                os.getStatus().getDescricao(),
                os.getVeiculo().getPlaca().getNumero(),
                os.getVeiculo().getModelo(),
                os.getOrcamentoStatus().name(),
                os.getServicos().stream().map(ItemServicoResponse::from).toList(),
                os.getPecas().stream().map(ItemPecaResponse::from).toList(),
                os.valorTotal(),
                os.getCriadaEm(),
                os.getInicioExecucaoEm(),
                os.getFimExecucaoEm(),
                os.getEntregueEm());
    }
}
