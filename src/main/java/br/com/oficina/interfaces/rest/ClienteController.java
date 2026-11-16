package br.com.oficina.interfaces.rest;

import br.com.oficina.application.dto.ClienteRequest;
import br.com.oficina.application.dto.ClienteResponse;
import br.com.oficina.application.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/clientes")
@Tag(name = "Clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra cliente (valida CPF/CNPJ)")
    public ClienteResponse criar(@Valid @RequestBody ClienteRequest request) {
        return service.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista clientes")
    public List<ClienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha cliente")
    public ClienteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza cliente")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove cliente (falha com 409 se houver veículos/OS vinculados)")
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
