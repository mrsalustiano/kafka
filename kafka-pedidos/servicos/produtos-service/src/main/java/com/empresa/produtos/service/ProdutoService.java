package com.empresa.produtos.service;

import com.empresa.produtos.audit.AuditoriaService;
import com.empresa.produtos.audit.OperacaoAuditoria;
import com.empresa.produtos.cache.ProdutoCacheService;
import com.empresa.produtos.dto.PageResponse;
import com.empresa.produtos.dto.ProdutoRequest;
import com.empresa.produtos.dto.ProdutoResponse;
import com.empresa.produtos.entity.Produto;
import com.empresa.produtos.exception.DatabaseException;
import com.empresa.produtos.exception.NotFoundException;
import com.empresa.produtos.exception.ValidationException;
import com.empresa.produtos.mapper.ProdutoMapper;
import com.empresa.produtos.repository.ProdutoRepository;
import com.empresa.produtos.util.PageResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;
    private final ProdutoCacheService produtoCacheService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        try {
            Produto produto = produtoMapper.toEntity(request);
            long id = produtoRepository.insert(produto);
            Produto salvo = produtoRepository.findAtivoById(id)
                    .orElseThrow(() -> new NotFoundException("Produto nao encontrado apos criacao"));
            ProdutoResponse response = produtoMapper.toResponse(salvo);
            produtoCacheService.salvarProduto(response);
            recarregarCacheCompleto();
            auditoriaService.registrar(OperacaoAuditoria.CREATE, id, "Produto criado: " + request.descricao());
            return response;
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao criar produto", ex);
        }
    }

    public ProdutoResponse buscarPorId(Long id) {
        Optional<ProdutoResponse> cache = produtoCacheService.buscarPorId(id);
        if (cache.isPresent()) {
            log.debug("Produto {} encontrado no Redis", id);
            return cache.get();
        }

        try {
            Produto produto = produtoRepository.findAtivoById(id)
                    .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + id));
            ProdutoResponse response = produtoMapper.toResponse(produto);
            produtoCacheService.salvarProduto(response);
            return response;
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao buscar produto: " + id, ex);
        }
    }

    public PageResponse<ProdutoResponse> listar(int page, int size, String sort) {
        try {
            String sortColumn = PageResponseUtil.resolveSortColumn(sort);
            String sortDirection = PageResponseUtil.resolveSortDirection(sort);
            long offset = (long) page * size;
            List<Produto> produtos = produtoRepository.findAllAtivosPaginado(sortColumn, sortDirection, size, offset);
            long total = produtoRepository.countAtivos();
            List<ProdutoResponse> content = produtoMapper.toResponseList(produtos);
            return PageResponseUtil.of(content, page, size, total);
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao listar produtos", ex);
        }
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        try {
            Produto existente = produtoRepository.findAtivoById(id)
                    .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + id));
            existente.setDescricao(request.descricao());
            existente.setValor(request.valor());
            int atualizados = produtoRepository.update(existente);
            if (atualizados == 0) {
                throw new NotFoundException("Produto nao encontrado: " + id);
            }
            Produto atualizado = produtoRepository.findAtivoById(id)
                    .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + id));
            ProdutoResponse response = produtoMapper.toResponse(atualizado);
            produtoCacheService.salvarProduto(response);
            recarregarCacheCompleto();
            auditoriaService.registrar(OperacaoAuditoria.UPDATE, id, "Produto atualizado: " + request.descricao());
            return response;
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao atualizar produto: " + id, ex);
        }
    }

    @Transactional
    public void excluirLogicamente(Long id) {
        try {
            produtoRepository.findAtivoById(id)
                    .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + id));
            int desativados = produtoRepository.desativar(id);
            if (desativados == 0) {
                throw new NotFoundException("Produto nao encontrado: " + id);
            }
            produtoCacheService.removerProduto(id);
            recarregarCacheCompleto();
            auditoriaService.registrar(OperacaoAuditoria.DELETE, id, "Exclusao logica do produto");
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao excluir produto: " + id, ex);
        }
    }

    public void recarregarCacheCompleto() {
        try {
            List<Produto> produtos = produtoRepository.findAllAtivos();
            List<ProdutoResponse> responses = produtoMapper.toResponseList(produtos);
            produtoCacheService.salvarTodos(responses);
            log.info("Cache de produtos recarregado com {} registros", responses.size());
        } catch (Exception ex) {
            log.warn("Falha ao recarregar cache completo de produtos: {}", ex.getMessage());
        }
    }
}
