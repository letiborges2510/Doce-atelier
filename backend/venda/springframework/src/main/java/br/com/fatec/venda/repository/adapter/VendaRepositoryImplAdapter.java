package br.com.fatec.venda.repository.adapter;

import br.com.fatec.venda.entity.ItemVenda;
import br.com.fatec.venda.entity.Venda;
import br.com.fatec.venda.repository.orm.ItemVendaOrm;
import br.com.fatec.venda.repository.orm.VendaOrm;

public class VendaRepositoryImplAdapter {
    private VendaRepositoryImplAdapter() {
    }

    public static Venda cast(VendaOrm orm) {
        return new Venda(
                orm.getId(),
                orm.getClienteId(),
                orm.getItens().stream().map(VendaRepositoryImplAdapter::cast).toList(),
                orm.getValorTotal(),
                orm.getStatus(),
                orm.getDataHora(),
                orm.getStatusAtualizadoEm());
    }

    public static VendaOrm cast(Venda venda) {
        return new VendaOrm(
                venda.id(),
                venda.clienteId(),
                venda.itens().stream().map(VendaRepositoryImplAdapter::cast).toList(),
                venda.valorTotal(),
                venda.status(),
                venda.dataHora(),
                venda.statusAtualizadoEm());
    }

    private static ItemVenda cast(ItemVendaOrm orm) {
        return new ItemVenda(orm.getProdutoId(), orm.getNomeProduto(), orm.getQuantidade(), orm.getPrecoUnitario());
    }

    private static ItemVendaOrm cast(ItemVenda item) {
        return new ItemVendaOrm(item.produtoId(), item.nomeProduto(), item.quantidade(), item.precoUnitario());
    }
}
