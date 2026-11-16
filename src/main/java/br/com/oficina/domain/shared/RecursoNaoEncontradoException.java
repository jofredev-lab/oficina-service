package br.com.oficina.domain.shared;

/** Agregado/entidade não encontrado (HTTP 404). */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
