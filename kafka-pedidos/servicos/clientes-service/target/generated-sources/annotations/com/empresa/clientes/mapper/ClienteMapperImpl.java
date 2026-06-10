package com.empresa.clientes.mapper;

import com.empresa.clientes.dto.ClienteRequest;
import com.empresa.clientes.dto.ClienteResponse;
import com.empresa.clientes.dto.ClienteStatusResponse;
import com.empresa.clientes.entity.Cliente;
import com.empresa.clientes.entity.ClienteStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-10T00:40:56-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class ClienteMapperImpl implements ClienteMapper {

    @Override
    public ClienteResponse toResponse(Cliente cliente) {
        if ( cliente == null ) {
            return null;
        }

        Long codigoCliente = null;
        String nome = null;
        String endereco = null;
        String cep = null;
        String cidade = null;
        String estado = null;
        String email = null;
        String telefone = null;
        String ativo = null;
        LocalDateTime dataCriacao = null;
        LocalDateTime dataAtualizacao = null;

        codigoCliente = cliente.getCodigoCliente();
        nome = cliente.getNome();
        endereco = cliente.getEndereco();
        cep = cliente.getCep();
        cidade = cliente.getCidade();
        estado = cliente.getEstado();
        email = cliente.getEmail();
        telefone = cliente.getTelefone();
        ativo = cliente.getAtivo();
        dataCriacao = cliente.getDataCriacao();
        dataAtualizacao = cliente.getDataAtualizacao();

        ClienteResponse clienteResponse = new ClienteResponse( codigoCliente, nome, endereco, cep, cidade, estado, email, telefone, ativo, dataCriacao, dataAtualizacao );

        return clienteResponse;
    }

    @Override
    public List<ClienteResponse> toResponseList(List<Cliente> clientes) {
        if ( clientes == null ) {
            return null;
        }

        List<ClienteResponse> list = new ArrayList<ClienteResponse>( clientes.size() );
        for ( Cliente cliente : clientes ) {
            list.add( toResponse( cliente ) );
        }

        return list;
    }

    @Override
    public ClienteStatusResponse toStatusResponse(ClienteStatus clienteStatus) {
        if ( clienteStatus == null ) {
            return null;
        }

        Long codigoCliente = null;
        String status = null;
        LocalDateTime dataAtualizacao = null;

        codigoCliente = clienteStatus.getCodigoCliente();
        status = clienteStatus.getStatus();
        dataAtualizacao = clienteStatus.getDataAtualizacao();

        ClienteStatusResponse clienteStatusResponse = new ClienteStatusResponse( codigoCliente, status, dataAtualizacao );

        return clienteStatusResponse;
    }

    @Override
    public Cliente toEntity(ClienteRequest request) {
        if ( request == null ) {
            return null;
        }

        Cliente.ClienteBuilder cliente = Cliente.builder();

        cliente.nome( request.nome() );
        cliente.endereco( request.endereco() );
        cliente.cep( request.cep() );
        cliente.cidade( request.cidade() );
        cliente.estado( request.estado() );
        cliente.email( request.email() );
        cliente.telefone( request.telefone() );

        cliente.ativo( "S" );

        return cliente.build();
    }
}
