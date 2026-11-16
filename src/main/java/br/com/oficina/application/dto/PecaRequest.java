package br.com.oficina.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PecaRequest(
        @NotBlank String codigo,
        @NotBlank String nome,
        @NotNull @DecimalMin("0.00") BigDecimal preco,
        @NotNull @Min(0) Integer quantidadeEstoque,
        @NotNull @Min(0) Integer estoqueMinimo) {
}
