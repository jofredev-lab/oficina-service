package br.com.oficina.interfaces.rest;

import br.com.oficina.application.dto.AcompanhamentoResponse;
import br.com.oficina.application.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/** API pública usada pelo aplicativo do cliente. O código de acompanhamento (UUID) é o segredo de acesso. */
@RestController
@RequestMapping("/api/public/acompanhamento")
@Tag(name = "Acompanhamento do cliente")
@SecurityRequirements
public class AcompanhamentoController {

    private final OrdemServicoService service;

    public AcompanhamentoController(OrdemServicoService service) {
        this.service = service;
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Consulta o andamento da OS e o orçamento")
    public AcompanhamentoResponse consultar(@PathVariable String codigo) {
        return service.consultarAcompanhamento(codigo);
    }

    @PostMapping("/{codigo}/orcamento/aprovar")
    @Operation(summary = "Cliente aprova o orçamento (inicia a execução e baixa o estoque)")
    public AcompanhamentoResponse aprovar(@PathVariable String codigo) {
        return service.aprovarPorCodigo(codigo);
    }

    @PostMapping("/{codigo}/orcamento/rejeitar")
    @Operation(summary = "Cliente rejeita o orçamento (OS é finalizada sem execução)")
    public AcompanhamentoResponse rejeitar(@PathVariable String codigo) {
        return service.rejeitarPorCodigo(codigo);
    }
}
