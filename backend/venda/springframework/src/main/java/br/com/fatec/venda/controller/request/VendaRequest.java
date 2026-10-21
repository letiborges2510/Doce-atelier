package br.com.fatec.venda.controller.request;

import java.util.List;

public record VendaRequest(List<ItemVendaRequest> itens) {
}
