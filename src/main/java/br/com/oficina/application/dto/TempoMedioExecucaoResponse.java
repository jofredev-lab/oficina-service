package br.com.oficina.application.dto;

public record TempoMedioExecucaoResponse(long quantidadeOrdens, double tempoMedioMinutos, String tempoMedioFormatado) {
}
