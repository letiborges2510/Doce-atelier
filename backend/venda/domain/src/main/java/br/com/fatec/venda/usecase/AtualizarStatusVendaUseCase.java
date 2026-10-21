package br.com.fatec.venda.usecase;

import br.com.fatec.venda.entity.Venda;
import br.com.fatec.venda.entity.enumerable.StatusVenda;
import br.com.fatec.venda.event.VendaPublisher;
import br.com.fatec.venda.repository.VendaRepository;

import java.time.LocalDateTime;

/**
 * Avança o status do pedido, um passo por vez:
 * PENDENTE -> CONFIRMADA -> SAIU_PARA_ENTREGA -> ENTREGUE.
 */
public class AtualizarStatusVendaUseCase {

    private final VendaRepository vendaRepository;
    private final VendaPublisher vendaPublisher;

    public AtualizarStatusVendaUseCase(VendaRepository vendaRepository, VendaPublisher vendaPublisher) {
        this.vendaRepository = vendaRepository;
        this.vendaPublisher = vendaPublisher;
    }

    public Venda executar(String vendaId, StatusVenda novoStatus) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));

        if (!venda.status().podeAvancarPara(novoStatus)) {
            throw new IllegalStateException("Transição inválida: " + venda.status() + " -> " + novoStatus);
        }

        Venda atualizada = new Venda(
                venda.id(), venda.clienteId(), venda.itens(), venda.valorTotal(), novoStatus,
                venda.dataHora(), LocalDateTime.now());
        Venda salva = vendaRepository.atualizarStatus(atualizada);
        // avisa o relatorio-service (RabbitMQ) que o status do pedido mudou
        vendaPublisher.publicar(salva);
        return salva;
    }
}
