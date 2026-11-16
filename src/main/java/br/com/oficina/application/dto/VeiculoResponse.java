package br.com.oficina.application.dto;

import br.com.oficina.domain.veiculo.Veiculo;

public record VeiculoResponse(Long id, String placa, String marca, String modelo, Integer ano, Long clienteId) {

    public static VeiculoResponse from(Veiculo v) {
        return new VeiculoResponse(v.getId(), v.getPlaca().getNumero(), v.getMarca(), v.getModelo(),
                v.getAno(), v.getCliente().getId());
    }
}
