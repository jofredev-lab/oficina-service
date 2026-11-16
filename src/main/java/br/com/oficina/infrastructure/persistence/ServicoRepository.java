package br.com.oficina.infrastructure.persistence;

import br.com.oficina.domain.servico.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
}
