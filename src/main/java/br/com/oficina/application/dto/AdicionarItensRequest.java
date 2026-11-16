package br.com.oficina.application.dto;

import jakarta.validation.Valid;

import java.util.List;

public record AdicionarItensRequest(List<Long> servicosIds, @Valid List<PecaItemRequest> pecas) {
}
