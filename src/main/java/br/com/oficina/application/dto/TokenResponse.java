package br.com.oficina.application.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos) {
}
