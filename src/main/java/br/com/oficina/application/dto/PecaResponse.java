package br.com.oficina.application.dto;

import br.com.oficina.domain.peca.Peca;

import java.math.BigDecimal;

public record PecaResponse(Long id, String codigo, String nome, BigDecimal preco,
                           int quantidadeEstoque, int estoqueMinimo, boolean estoqueBaixo) {

    public static PecaResponse from(Peca p) {
        return new PecaResponse(p.getId(), p.getCodigo(), p.getNome(), p.getPreco(),
                p.getQuantidadeEstoque(), p.getEstoqueMinimo(), p.isEstoqueBaixo());
    }
}
