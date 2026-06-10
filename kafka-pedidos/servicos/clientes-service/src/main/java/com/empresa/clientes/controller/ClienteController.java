package com.empresa.clientes.controller;

import com.empresa.clientes.dto.ClienteAcceptedResponse;
import com.empresa.clientes.dto.ClienteRequest;
import com.empresa.clientes.dto.ClienteResponse;
import com.empresa.clientes.dto.ClienteStatusResponse;
import com.empresa.clientes.dto.PageResponse;
import com.empresa.clientes.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Operacoes de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Solicitar criacao de cliente via Kafka")
    public ResponseEntity<ClienteAcceptedResponse> solicitarCriacao(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(clienteService.solicitarCriacao(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar clientes paginado")
    public ResponseEntity<PageResponse<ClienteResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(clienteService.listar(page, size, sort));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclusao logica de cliente")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluirLogicamente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{id}")
    @Operation(summary = "Consultar status de criacao do cliente")
    public ResponseEntity<ClienteStatusResponse> consultarStatus(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.consultarStatus(id));
    }
}
