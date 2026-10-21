package br.com.fatec.venda.event;

import br.com.fatec.venda.entity.Venda;

public interface VendaPublisher {
    void publicar(Venda venda);
}
