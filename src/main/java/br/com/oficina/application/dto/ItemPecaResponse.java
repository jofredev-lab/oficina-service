package br.com.oficina.application.dto;

import br.com.oficina.domain.ordemservico.ItemPeca;

import java.math.BigDecimal;

public record ItemPecaResponse(Long pecaId, String nome, BigDecimal precoUnitario, int quantidade,
                               BigDecimal subtotal) {

    public static ItemPecaResponse from(ItemPeca i) {
        return new ItemPecaResponse(i.getPeca().getId(), i.getNomePeca(), i.getPrecoUnitario(),
                i.getQuantidade(), i.subtotal());
    }
}
