package br.com.fatec.venda.controller.response;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime date, String path, Integer status, String error, String message) {
}
