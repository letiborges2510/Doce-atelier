package br.com.fatec.venda.repository;

import br.com.fatec.venda.entity.Venda;
import br.com.fatec.venda.entity.enumerable.StatusVenda;

import java.util.List;
import java.util.Optional;

public interface VendaRepository {
    Venda save(Venda venda);

    // Atualiza o status de uma venda que já existe (save() só insere).
    Venda atualizarStatus(Venda venda);

    Optional<Venda> findById(String id);

    List<Venda> findByClienteId(String clienteId);

    List<Venda> findByStatus(StatusVenda status);

    List<Venda> findByStatusIn(List<StatusVenda> status);

    // Lista todas as vendas, de todos os clientes - usado pelo painel
    // admin do front (aba "Vendas e pedidos").
    List<Venda> findAll();
}
