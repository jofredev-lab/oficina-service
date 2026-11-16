package br.com.oficina.application.dto;

import br.com.oficina.domain.cliente.Cliente;

public record ClienteResponse(Long id, String nome, String documento, String tipoDocumento,
                              String email, String telefone) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNome(), c.getDocumento().getNumero(),
                c.getDocumento().getTipo().name(), c.getEmail(), c.getTelefone());
    }
}
