package br.com.oficina.application.dto;

import br.com.oficina.domain.servico.Servico;

import java.math.BigDecimal;

public record ServicoResponse(Long id, String nome, String descricao, BigDecimal preco) {

    public static ServicoResponse from(Servico s) {
        return new ServicoResponse(s.getId(), s.getNome(), s.getDescricao(), s.getPreco());
    }
}
