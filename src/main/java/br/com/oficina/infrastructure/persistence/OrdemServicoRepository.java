package br.com.oficina.infrastructure.persistence;

import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.ordemservico.StatusOS;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    Optional<OrdemServico> findByCodigoAcompanhamento(String codigoAcompanhamento);

    /** Fila de atendimento: mais antigas primeiro (FIFO). */
    List<OrdemServico> findAllByOrderByCriadaEmAsc();

    List<OrdemServico> findByStatusOrderByCriadaEmAsc(StatusOS status);

    List<OrdemServico> findByInicioExecucaoEmIsNotNullAndFimExecucaoEmIsNotNull();
}
