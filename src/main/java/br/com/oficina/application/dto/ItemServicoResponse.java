package br.com.oficina.application.dto;

import br.com.oficina.domain.ordemservico.ItemServico;

import java.math.BigDecimal;

public record ItemServicoResponse(Long servicoId, String nome, BigDecimal preco) {

    public static ItemServicoResponse from(ItemServico i) {
        return new ItemServicoResponse(i.getServico().getId(), i.getNomeServico(), i.getPreco());
    }
}
