package br.com.fatec.relatorio.messaging;

import br.com.fatec.relatorio.usecase.RegistrarVendaUseCase;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class VendaEventoConsumer {

    private final RegistrarVendaUseCase registrarVendaUseCase;

    public VendaEventoConsumer(RegistrarVendaUseCase registrarVendaUseCase) {
        this.registrarVendaUseCase = registrarVendaUseCase;
    }

    @RabbitListener(queues = "${event.venda.queue}")
    public void receberEvento(VendaEventoRecebido evento) {
        // Chega um evento quando a venda é criada (PENDENTE) e outro a cada mudança de status.
        registrarVendaUseCase.executar(evento.getVendaId(), evento.getValorTotal(), evento.getStatus());
        System.out.println("[relatorio] Pedido " + evento.getVendaId() + " -> " + evento.getStatus()
                + " (R$ " + evento.getValorTotal() + ")");
    }
}
