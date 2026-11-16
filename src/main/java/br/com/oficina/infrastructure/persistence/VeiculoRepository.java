package br.com.oficina.infrastructure.persistence;

import br.com.oficina.domain.veiculo.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    Optional<Veiculo> findByPlacaNumero(String numero);

    List<Veiculo> findByClienteId(Long clienteId);
}
