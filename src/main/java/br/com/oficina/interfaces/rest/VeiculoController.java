package br.com.oficina.interfaces.rest;

import br.com.oficina.application.dto.VeiculoRequest;
import br.com.oficina.application.dto.VeiculoResponse;
import br.com.oficina.application.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/veiculos")
@Tag(name = "Veículos")
public class VeiculoController {

    private final VeiculoService service;

    public VeiculoController(VeiculoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra veículo (valida placa)")
    public VeiculoResponse criar(@Valid @RequestBody VeiculoRequest request) {
        return service.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista veículos, com filtro opcional por cliente")
    public List<VeiculoResponse> listar(@RequestParam(required = false) Long clienteId) {
        return service.listar(clienteId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha veículo")
    public VeiculoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza veículo")
    public VeiculoResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove veículo")
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
