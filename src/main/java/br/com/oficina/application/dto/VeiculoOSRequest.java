package br.com.oficina.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Dados do veículo informados na abertura da OS (cadastra o veículo se a placa ainda não existir). */
public record VeiculoOSRequest(
        @NotBlank String placa,
        @NotBlank String marca,
        @NotBlank String modelo,
        @NotNull Integer ano) {
}
