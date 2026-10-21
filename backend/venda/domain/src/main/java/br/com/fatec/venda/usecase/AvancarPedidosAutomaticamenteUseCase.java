package br.com.fatec.venda.usecase;

import br.com.fatec.venda.entity.Venda;
import br.com.fatec.venda.entity.enumerable.StatusVenda;
import br.com.fatec.venda.repository.VendaRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A API é quem controla o andamento dos pedidos: quando um pedido fica
 * tempoPorStatus no mesmo status, ele avança para o próximo
 * (PENDENTE -> CONFIRMADA -> SAIU_PARA_ENTREGA -> ENTREGUE).
 * Quem chama é o agendador do venda-service (a cada poucos segundos).
 */
public class AvancarPedidosAutomaticamenteUseCase {

    private final VendaRepository vendaRepository;
    private final AtualizarStatusVendaUseCase atualizarStatusVendaUseCase;

    public AvancarPedidosAutomaticamenteUseCase(
            VendaRepository vendaRepository, AtualizarStatusVendaUseCase atualizarStatusVendaUseCase) {
        this.vendaRepository = vendaRepository;
        this.atualizarStatusVendaUseCase = atualizarStatusVendaUseCase;
    }

    /** Devolve quantos pedidos avançaram nesta rodada. */
    public int executar(Duration tempoPorStatus) {
        LocalDateTime agora = LocalDateTime.now();
        int avancados = 0;
        List<Venda> emAndamento = vendaRepository.findByStatusIn(
                List.of(StatusVenda.PENDENTE, StatusVenda.CONFIRMADA, StatusVenda.SAIU_PARA_ENTREGA));
        for (Venda venda : emAndamento) {
            if (Duration.between(venda.desde(), agora).compareTo(tempoPorStatus) >= 0) {
                StatusVenda proximo = venda.status().proximo();
                atualizarStatusVendaUseCase.executar(venda.id(), proximo);
                System.out.println("[venda] Pedido " + venda.id() + ": " + venda.status() + " -> " + proximo);
                avancados++;
            }
        }
        return avancados;
    }
}
