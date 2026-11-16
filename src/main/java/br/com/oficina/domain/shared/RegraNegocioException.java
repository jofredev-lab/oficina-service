package br.com.oficina.domain.shared;

/** Violação de uma regra de negócio do domínio (HTTP 422). */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
