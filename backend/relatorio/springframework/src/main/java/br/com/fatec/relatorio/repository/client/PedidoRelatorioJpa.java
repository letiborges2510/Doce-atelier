package br.com.fatec.relatorio.repository.client;

import br.com.fatec.relatorio.repository.orm.PedidoRelatorioOrm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PedidoRelatorioJpa extends JpaRepository<PedidoRelatorioOrm, String> {

    @Query("select sum(p.valorTotal) from PedidoRelatorioOrm p where p.status <> 'RECUSADA'")
    BigDecimal faturamento(); // null quando não há pedidos

    @Query("select count(p) from PedidoRelatorioOrm p where p.status <> 'RECUSADA'")
    long pedidosValidos();

    @Query("select p.status, count(p) from PedidoRelatorioOrm p group by p.status")
    List<Object[]> contagemPorStatus();
}
