package br.com.fatec.venda.usecase;

import br.com.fatec.venda.entity.ItemVenda;
import br.com.fatec.venda.entity.Venda;
import br.com.fatec.venda.entity.enumerable.StatusVenda;
import br.com.fatec.venda.event.VendaPublisher;
import br.com.fatec.venda.gateway.EstoqueGateway;
import br.com.fatec.venda.repository.VendaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Regra de negocio da venda.
 *
 * Para cada item pedido, chama o gestao-service (EstoqueGateway) pra
 * dar baixa no estoque - é LÁ que mora o lock pessimista que resolve
 * a concorrencia, já que é o gestao quem é dono do dado "estoque".
 * Se faltar estoque de QUALQUER item, a chamada estoura e a venda
 * inteira é cancelada (nenhum item fica "meio vendido").
 *
 * Depois de confirmada e salva, a venda é publicada (VendaPublisher)
 * para quem quiser ser notificado de forma assincrona - no caso, o
 * relatorio-service, que atualiza o faturamento sem o cliente
 * precisar esperar essa atualização terminar.
 */
public class RealizarVendaUseCase {

    private final EstoqueGateway estoqueGateway;
    private final VendaRepository vendaRepository;
    private final VendaPublisher vendaPublisher;

    public RealizarVendaUseCase(
            EstoqueGateway estoqueGateway,
            VendaRepository vendaRepository,
            VendaPublisher vendaPublisher) {
        this.estoqueGateway = estoqueGateway;
        this.vendaRepository = vendaRepository;
        this.vendaPublisher = vendaPublisher;
    }

    public Venda executar(String clienteId, List<ItemPedido> itensPedidos) {
        if (itensPedidos == null || itensPedidos.isEmpty()) {
            throw new IllegalArgumentException("A venda precisa ter ao menos um item");
        }

        List<ItemVenda> itens = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;

        try {
            for (ItemPedido pedido : itensPedidos) {
                if (pedido.quantidade() <= 0) {
                    throw new IllegalArgumentException("A quantidade precisa ser maior que zero");
                }
                EstoqueGateway.InfoProduto info = estoqueGateway.baixarEstoque(pedido.produtoId(), pedido.quantidade());
                // só entra na lista depois da baixa: é a lista do que precisa ser devolvido se algo falhar
                ItemVenda item = new ItemVenda(pedido.produtoId(), info.nome(), pedido.quantidade(), info.preco());
                itens.add(item);
                valorTotal = valorTotal.add(item.subtotal());
            }

            LocalDateTime agora = LocalDateTime.now();
            Venda venda = new Venda(
                    UUID.randomUUID().toString(),
                    clienteId,
                    itens,
                    valorTotal,
                    StatusVenda.PENDENTE,
                    agora,
                    agora);

            Venda vendaSalva = vendaRepository.save(venda);
            vendaPublisher.publicar(vendaSalva);
            return vendaSalva;
        } catch (RuntimeException ex) {
            // Cada baixa é uma transação própria no gestao-service (já foi confirmada lá).
            // Se algo falhou no meio do pedido, devolvemos o que já tinha sido descontado,
            // para nenhum item ficar "meio vendido".
            devolverEstoque(itens);
            throw ex;
        }
    }

    private void devolverEstoque(List<ItemVenda> itensJaBaixados) {
        for (ItemVenda item : itensJaBaixados) {
            try {
                estoqueGateway.reporEstoque(item.produtoId(), item.quantidade());
            } catch (RuntimeException falha) {
                System.err.println("[venda] ATENÇÃO: não consegui devolver " + item.quantidade()
                        + " un. do produto " + item.produtoId() + " ao estoque: " + falha.getMessage());
            }
        }
    }

    public record ItemPedido(String produtoId, int quantidade) {
    }
}
