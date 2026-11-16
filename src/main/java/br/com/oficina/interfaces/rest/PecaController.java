package br.com.oficina.interfaces.rest;

import br.com.oficina.application.dto.AjusteEstoqueRequest;
import br.com.oficina.application.dto.PecaRequest;
import br.com.oficina.application.dto.PecaResponse;
import br.com.oficina.application.service.PecaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pecas")
@Tag(name = "Peças e insumos")
public class PecaController {

    private final PecaService service;

    public PecaController(PecaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra peça/insumo")
    public PecaResponse criar(@Valid @RequestBody PecaRequest request) {
        return service.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista peças e insumos")
    public List<PecaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/estoque-baixo")
    @Operation(summary = "Lista peças com estoque igual ou abaixo do mínimo")
    public List<PecaResponse> estoqueBaixo() {
        return service.listarComEstoqueBaixo();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha peça")
    public PecaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza peça (inclui correção de estoque)")
    public PecaResponse atualizar(@PathVariable Long id, @Valid @RequestBody PecaRequest request) {
        return service.atualizar(id, request);
    }

    @PostMapping("/{id}/estoque/entrada")
    @Operation(summary = "Registra entrada de estoque")
    public PecaResponse entrada(@PathVariable Long id, @Valid @RequestBody AjusteEstoqueRequest request) {
        return service.registrarEntrada(id, request.quantidade());
    }

    @PostMapping("/{id}/estoque/saida")
    @Operation(summary = "Registra saída manual de estoque (falha se não houver saldo)")
    public PecaResponse saida(@PathVariable Long id, @Valid @RequestBody AjusteEstoqueRequest request) {
        return service.registrarSaida(id, request.quantidade());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove peça")
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
