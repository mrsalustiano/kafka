package com.empresa.pedidos.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        int numberOfElements,
        boolean first,
        boolean last,
        boolean empty,
        boolean hasNext,
        boolean hasPrevious
) {
}
