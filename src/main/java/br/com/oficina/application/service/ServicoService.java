package br.com.oficina.application.service;

import br.com.oficina.application.dto.ServicoRequest;
import br.com.oficina.application.dto.ServicoResponse;
import br.com.oficina.domain.servico.Servico;
import br.com.oficina.domain.shared.RecursoNaoEncontradoException;
import br.com.oficina.infrastructure.persistence.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicos;

    public ServicoService(ServicoRepository servicos) {
        this.servicos = servicos;
    }

    @Transactional
    public ServicoResponse criar(ServicoRequest r) {
        return ServicoResponse.from(servicos.save(new Servico(r.nome(), r.descricao(), r.preco())));
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listar() {
        return servicos.findAll().stream().map(ServicoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscar(Long id) {
        return ServicoResponse.from(obter(id));
    }

    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest r) {
        Servico servico = obter(id);
        servico.atualizar(r.nome(), r.descricao(), r.preco());
        return ServicoResponse.from(servico);
    }

    @Transactional
    public void remover(Long id) {
        servicos.delete(obter(id));
        servicos.flush();
    }

    private Servico obter(Long id) {
        return servicos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado: " + id));
    }
}
