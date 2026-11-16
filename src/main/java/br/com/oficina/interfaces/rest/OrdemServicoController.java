package br.com.oficina.interfaces.rest;

import br.com.oficina.application.dto.AbrirOrdemServicoRequest;
import br.com.oficina.application.dto.AdicionarItensRequest;
import br.com.oficina.application.dto.OrdemServicoResponse;
import br.com.oficina.application.service.OrdemServicoService;
import br.com.oficina.domain.ordemservico.StatusOS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ordens-servico")
@Tag(name = "Ordens de serviço")
public class OrdemServicoController {

    private final OrdemServicoService service;

    public OrdemServicoController(OrdemServicoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abre uma OS: identifica o cliente por CPF/CNPJ, cadastra/reutiliza o veículo e inclui serviços e peças")
    public OrdemServicoResponse abrir(@Valid @RequestBody AbrirOrdemServicoRequest request) {
        return service.abrir(request);
    }

    @GetMapping
    @Operation(summary = "Lista OS em ordem de chegada, com filtro opcional por status")
    public List<OrdemServicoResponse> listar(@RequestParam(required = false) StatusOS status) {
        return service.listar(status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha OS (itens, orçamento e datas)")
    public OrdemServicoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/itens")
    @Operation(summary = "Inclui serviços/peças (somente em Recebida ou Em diagnóstico)")
    public OrdemServicoResponse adicionarItens(@PathVariable Long id, @Valid @RequestBody AdicionarItensRequest request) {
        return service.adicionarItens(id, request);
    }

    @PostMapping("/{id}/diagnostico")
    @Operation(summary = "Recebida -> Em diagnóstico")
    public OrdemServicoResponse iniciarDiagnostico(@PathVariable Long id) {
        return service.iniciarDiagnostico(id);
    }

    @PostMapping("/{id}/orcamento/enviar")
    @Operation(summary = "Gera o orçamento e envia ao cliente: Em diagnóstico -> Aguardando aprovação")
    public OrdemServicoResponse enviarOrcamento(@PathVariable Long id) {
        return service.enviarOrcamento(id);
    }

    @PostMapping("/{id}/orcamento/aprovar")
    @Operation(summary = "Registra aprovação (balcão): Aguardando aprovação -> Em execução, com baixa de estoque")
    public OrdemServicoResponse aprovar(@PathVariable Long id) {
        return service.aprovarOrcamento(id);
    }

    @PostMapping("/{id}/orcamento/rejeitar")
    @Operation(summary = "Registra rejeição (balcão): Aguardando aprovação -> Finalizada, sem execução")
    public OrdemServicoResponse rejeitar(@PathVariable Long id) {
        return service.rejeitarOrcamento(id);
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Em execução -> Finalizada")
    public OrdemServicoResponse finalizar(@PathVariable Long id) {
        return service.finalizar(id);
    }

    @PostMapping("/{id}/entregar")
    @Operation(summary = "Finalizada -> Entregue")
    public OrdemServicoResponse entregar(@PathVariable Long id) {
        return service.entregar(id);
    }
}
