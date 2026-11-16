package br.com.oficina.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AbrirOrdemServicoRequest(
        @NotBlank String documentoCliente,
        @NotNull @Valid VeiculoOSRequest veiculo,
        List<Long> servicosIds,
        @Valid List<PecaItemRequest> pecas) {
}
