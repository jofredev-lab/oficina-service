package br.com.oficina.application.service;

import br.com.oficina.application.dto.VeiculoRequest;
import br.com.oficina.application.dto.VeiculoResponse;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.shared.ConflitoException;
import br.com.oficina.domain.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import br.com.oficina.infrastructure.persistence.ClienteRepository;
import br.com.oficina.infrastructure.persistence.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculos;
    private final ClienteRepository clientes;

    public VeiculoService(VeiculoRepository veiculos, ClienteRepository clientes) {
        this.veiculos = veiculos;
        this.clientes = clientes;
    }

    @Transactional
    public VeiculoResponse criar(VeiculoRequest r) {
        Placa placa = Placa.of(r.placa());
        if (veiculos.findByPlacaNumero(placa.getNumero()).isPresent()) {
            throw new ConflitoException("Já existe um veículo com esta placa");
        }
        Cliente cliente = obterCliente(r.clienteId());
        return VeiculoResponse.from(veiculos.save(new Veiculo(placa, r.marca(), r.modelo(), r.ano(), cliente)));
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listar(Long clienteId) {
        List<Veiculo> lista = clienteId == null ? veiculos.findAll() : veiculos.findByClienteId(clienteId);
        return lista.stream().map(VeiculoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscar(Long id) {
        return VeiculoResponse.from(obter(id));
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest r) {
        Veiculo veiculo = obter(id);
        Placa placa = Placa.of(r.placa());
        veiculos.findByPlacaNumero(placa.getNumero())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new ConflitoException("Já existe outro veículo com esta placa");
                });
        veiculo.atualizar(placa, r.marca(), r.modelo(), r.ano(), obterCliente(r.clienteId()));
        return VeiculoResponse.from(veiculo);
    }

    @Transactional
    public void remover(Long id) {
        veiculos.delete(obter(id));
        veiculos.flush();
    }

    private Veiculo obter(Long id) {
        return veiculos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado: " + id));
    }

    private Cliente obterCliente(Long clienteId) {
        return clientes.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + clienteId));
    }
}
