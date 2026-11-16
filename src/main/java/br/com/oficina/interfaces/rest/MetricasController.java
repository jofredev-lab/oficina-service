package br.com.oficina.interfaces.rest;

import br.com.oficina.application.dto.TempoMedioExecucaoResponse;
import br.com.oficina.application.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/metricas")
@Tag(name = "Métricas")
public class MetricasController {

    private final OrdemServicoService service;

    public MetricasController(OrdemServicoService service) {
        this.service = service;
    }

    @GetMapping("/tempo-medio-execucao")
    @Operation(summary = "Tempo médio de execução (da aprovação à finalização) das OS executadas")
    public TempoMedioExecucaoResponse tempoMedioExecucao() {
        return service.tempoMedioExecucao();
    }
}
