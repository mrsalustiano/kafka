package com.empresa.pedidos.service;

import com.empresa.pedidos.cache.ProdutoCacheService;
import com.empresa.pedidos.dto.ProdutoCacheDto;
import com.empresa.pedidos.entity.Produto;
import com.empresa.pedidos.exception.DatabaseException;
import com.empresa.pedidos.exception.ValidationException;
import com.empresa.pedidos.mapper.PedidoMapper;
import com.empresa.pedidos.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProdutoConsultaService {

    private static final String ATIVO = "S";

    private final ProdutoCacheService produtoCacheService;
    private final ProdutoRepository produtoRepository;
    private final PedidoMapper pedidoMapper;

    public ProdutoCacheDto buscarProdutoAtivo(Long codigoProduto) {
        var cache = produtoCacheService.buscarPorId(codigoProduto);
        if (cache.isPresent()) {
            ProdutoCacheDto produto = cache.get();
            validarAtivo(produto, codigoProduto);
            log.debug("Produto {} encontrado no Redis", codigoProduto);
            return produto;
        }

        try {
            Produto produto = produtoRepository.findAtivoById(codigoProduto)
                    .orElseThrow(() -> new ValidationException(
                            "Produto inativo ou nao encontrado: " + codigoProduto));
            ProdutoCacheDto cacheDto = pedidoMapper.toCacheDto(produto);
            produtoCacheService.salvarProduto(cacheDto);
            log.debug("Produto {} carregado do banco e atualizado no Redis", codigoProduto);
            return cacheDto;
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao consultar produto: " + codigoProduto, ex);
        }
    }

    private void validarAtivo(ProdutoCacheDto produto, Long codigoProduto) {
        if (!ATIVO.equals(produto.ativo())) {
            throw new ValidationException("Produto inativo ou nao encontrado: " + codigoProduto);
        }
    }
}
