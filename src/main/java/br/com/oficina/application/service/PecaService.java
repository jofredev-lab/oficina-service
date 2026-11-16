package br.com.oficina.application.service;

import br.com.oficina.application.dto.PecaRequest;
import br.com.oficina.application.dto.PecaResponse;
import br.com.oficina.domain.peca.Peca;
import br.com.oficina.domain.shared.ConflitoException;
import br.com.oficina.domain.shared.RecursoNaoEncontradoException;
import br.com.oficina.infrastructure.persistence.PecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PecaService {

    private final PecaRepository pecas;

    public PecaService(PecaRepository pecas) {
        this.pecas = pecas;
    }

    @Transactional
    public PecaResponse criar(PecaRequest r) {
        Peca nova = new Peca(r.codigo(), r.nome(), r.preco(), r.quantidadeEstoque(), r.estoqueMinimo());
        if (pecas.findByCodigo(nova.getCodigo()).isPresent()) {
            throw new ConflitoException("Já existe uma peça com o código " + nova.getCodigo());
        }
        return PecaResponse.from(pecas.save(nova));
    }

    @Transactional(readOnly = true)
    public List<PecaResponse> listar() {
        return pecas.findAll().stream().map(PecaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PecaResponse> listarComEstoqueBaixo() {
        return pecas.findAll().stream().filter(Peca::isEstoqueBaixo).map(PecaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PecaResponse buscar(Long id) {
        return PecaResponse.from(obter(id));
    }

    @Transactional
    public PecaResponse atualizar(Long id, PecaRequest r) {
        Peca peca = obter(id);
        String codigo = r.codigo().trim().toUpperCase();
        pecas.findByCodigo(codigo)
                .filter(outra -> !outra.getId().equals(id))
                .ifPresent(outra -> {
                    throw new ConflitoException("Já existe outra peça com o código " + codigo);
                });
        peca.atualizar(r.codigo(), r.nome(), r.preco(), r.quantidadeEstoque(), r.estoqueMinimo());
        return PecaResponse.from(peca);
    }

    @Transactional
    public PecaResponse registrarEntrada(Long id, int quantidade) {
        Peca peca = obter(id);
        peca.reporEstoque(quantidade);
        return PecaResponse.from(peca);
    }

    @Transactional
    public PecaResponse registrarSaida(Long id, int quantidade) {
        Peca peca = obter(id);
        peca.baixarEstoque(quantidade);
        return PecaResponse.from(peca);
    }

    @Transactional
    public void remover(Long id) {
        pecas.delete(obter(id));
        pecas.flush();
    }

    private Peca obter(Long id) {
        return pecas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada: " + id));
    }
}
