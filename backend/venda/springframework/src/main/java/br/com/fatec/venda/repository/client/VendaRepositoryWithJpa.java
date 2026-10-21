package br.com.fatec.venda.repository.client;

import br.com.fatec.venda.entity.enumerable.StatusVenda;
import br.com.fatec.venda.repository.orm.VendaOrm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VendaRepositoryWithJpa extends JpaRepository<VendaOrm, String> {
    List<VendaOrm> findByClienteId(String clienteId);

    List<VendaOrm> findByStatus(StatusVenda status);

    List<VendaOrm> findByStatusIn(Collection<StatusVenda> status);
}
