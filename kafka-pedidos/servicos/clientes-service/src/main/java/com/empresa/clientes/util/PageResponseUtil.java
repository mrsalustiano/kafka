package com.empresa.clientes.util;

import com.empresa.clientes.dto.PageResponse;
import com.empresa.clientes.exception.ValidationException;

import java.util.List;
import java.util.Set;

public final class PageResponseUtil {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "codigo_cliente", "nome", "cpf", "email", "cidade", "data_criacao", "data_atualizacao"
    );

    private PageResponseUtil() {
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int numberOfElements = content.size();
        boolean empty = numberOfElements == 0;
        boolean first = page == 0;
        boolean last = page >= totalPages - 1 || totalPages == 0;
        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                numberOfElements,
                first,
                last,
                empty,
                !last,
                !first
        );
    }

    public static String resolveSortColumn(String sort) {
        if (sort == null || sort.isBlank()) {
            return "codigo_cliente";
        }
        String column = sort.split(",")[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(column)) {
            throw new ValidationException("Campo de ordenacao invalido: " + column);
        }
        return column;
    }

    public static String resolveSortDirection(String sort) {
        if (sort == null || !sort.contains(",")) {
            return "ASC";
        }
        String direction = sort.split(",")[1].trim().toUpperCase();
        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
            throw new ValidationException("Direcao de ordenacao invalida: " + direction);
        }
        return direction;
    }
}
