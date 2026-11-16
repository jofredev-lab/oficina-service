package br.com.oficina.interfaces.rest;

import br.com.oficina.application.dto.ServicoRequest;
import br.com.oficina.application.dto.ServicoResponse;
import br.com.oficina.application.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/servicos")
@Tag(name = "Serviços")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra serviço do catálogo")
    public ServicoResponse criar(@Valid @RequestBody ServicoRequest request) {
        return service.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista serviços")
    public List<ServicoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha serviço")
    public ServicoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza serviço")
    public ServicoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ServicoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove serviço")
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
