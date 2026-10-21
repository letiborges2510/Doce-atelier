package br.com.fatec.venda.controller.adapter;

import br.com.fatec.venda.controller.response.ItemVendaResponse;
import br.com.fatec.venda.controller.response.VendaResponse;
import br.com.fatec.venda.entity.Venda;

public class VendaControllerAdapter {
    private VendaControllerAdapter() {
    }

    public static VendaResponse cast(Venda venda) {
        return new VendaResponse(
                venda.id(),
                venda.clienteId(),
                venda.itens().stream()
                        .map(item -> new ItemVendaResponse(item.produtoId(), item.nomeProduto(), item.quantidade(), item.precoUnitario()))
                        .toList(),
                venda.valorTotal(),
                venda.status().name(),
                venda.dataHora());
    }
}
