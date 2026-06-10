package com.empresa.clientes.mapper;

import com.empresa.clientes.dto.ClienteRequest;
import com.empresa.clientes.dto.ClienteResponse;
import com.empresa.clientes.dto.ClienteStatusResponse;
import com.empresa.clientes.entity.Cliente;
import com.empresa.clientes.entity.ClienteStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteResponse toResponse(Cliente cliente);

    List<ClienteResponse> toResponseList(List<Cliente> clientes);

    ClienteStatusResponse toStatusResponse(ClienteStatus clienteStatus);

    @Mapping(target = "codigoCliente", ignore = true)
    @Mapping(target = "ativo", constant = "S")
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Cliente toEntity(ClienteRequest request);
}
