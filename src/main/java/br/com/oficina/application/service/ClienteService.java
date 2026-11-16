package br.com.oficina.application.service;

import br.com.oficina.application.dto.ClienteRequest;
import br.com.oficina.application.dto.ClienteResponse;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.shared.ConflitoException;
import br.com.oficina.domain.shared.RecursoNaoEncontradoException;
import br.com.oficina.infrastructure.persistence.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest r) {
        Documento documento = Documento.of(r.documento());
        if (clientes.findByDocumentoNumero(documento.getNumero()).isPresent()) {
            throw new ConflitoException("Já existe um cliente com este CPF/CNPJ");
        }
        Cliente cliente = clientes.save(new Cliente(r.nome(), documento, r.email(), r.telefone()));
        return ClienteResponse.from(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clientes.findAll().stream().map(ClienteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.from(obter(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest r) {
        Cliente cliente = obter(id);
        Documento documento = Documento.of(r.documento());
        clientes.findByDocumentoNumero(documento.getNumero())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new ConflitoException("Já existe outro cliente com este CPF/CNPJ");
                });
        cliente.atualizar(r.nome(), documento, r.email(), r.telefone());
        return ClienteResponse.from(cliente);
    }

    @Transactional
    public void remover(Long id) {
        clientes.delete(obter(id));
        clientes.flush();
    }

    private Cliente obter(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + id));
    }
}
