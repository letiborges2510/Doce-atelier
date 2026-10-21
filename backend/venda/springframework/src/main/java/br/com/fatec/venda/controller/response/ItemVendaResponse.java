package br.com.fatec.venda.controller.response;

import java.math.BigDecimal;

public record ItemVendaResponse(String produtoId, String nomeProduto, int quantidade, BigDecimal precoUnitario) {
}
