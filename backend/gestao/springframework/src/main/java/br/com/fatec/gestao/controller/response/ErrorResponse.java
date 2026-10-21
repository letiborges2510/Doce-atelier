package br.com.fatec.gestao.controller.response;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime date,
        String path,
        Integer status,
        String error,
        String message
) {
}
