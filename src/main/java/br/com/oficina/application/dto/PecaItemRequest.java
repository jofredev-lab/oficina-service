package br.com.oficina.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PecaItemRequest(@NotNull Long pecaId, @NotNull @Min(1) Integer quantidade) {
}
