package br.com.oficina.domain.shared;

/** Conflito com o estado atual (duplicidade, registro em uso) (HTTP 409). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
