package br.com.fatec.venda.repository;

import br.com.fatec.venda.entity.Venda;
import br.com.fatec.venda.entity.enumerable.StatusVenda;
import br.com.fatec.venda.repository.adapter.VendaRepositoryImplAdapter;
import br.com.fatec.venda.repository.client.VendaRepositoryWithJpa;
import br.com.fatec.venda.repository.orm.VendaOrm;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Transactional na classe: a lista de itens da venda é carregada sob demanda, então
// precisa de sessão aberta também fora do controller (ex.: no agendador).
@Repository
@Transactional
public class VendaRepositoryImpl implements VendaRepository {

    private final VendaRepositoryWithJpa repository;

    public VendaRepositoryImpl(VendaRepositoryWithJpa repository) {
        this.repository = repository;
    }

    @Override
    public Venda save(Venda venda) {
        try {
            VendaOrm orm = repository.save(VendaRepositoryImplAdapter.cast(venda));
            return VendaRepositoryImplAdapter.cast(orm);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Venda atualizarStatus(Venda venda) {
        // VendaOrm.isNew() é sempre true (save() = insert), então o update
        // é feito alterando a entidade carregada (gerenciada) dentro da
        // transação do controller; o commit grava a mudança.
        VendaOrm orm = repository.findById(venda.id())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
        orm.setStatus(venda.status());
        orm.setStatusAtualizadoEm(venda.statusAtualizadoEm());
        return VendaRepositoryImplAdapter.cast(orm);
    }

    @Override
    public Optional<Venda> findById(String id) {
        return repository.findById(id).map(VendaRepositoryImplAdapter::cast);
    }

    @Override
    public List<Venda> findByClienteId(String clienteId) {
        return repository.findByClienteId(clienteId).stream().map(VendaRepositoryImplAdapter::cast).toList();
    }

    @Override
    public List<Venda> findByStatus(StatusVenda status) {
        return repository.findByStatus(status).stream().map(VendaRepositoryImplAdapter::cast).toList();
    }

    @Override
    public List<Venda> findByStatusIn(List<StatusVenda> status) {
        return repository.findByStatusIn(status).stream().map(VendaRepositoryImplAdapter::cast).toList();
    }

    @Override
    public List<Venda> findAll() {
        return repository.findAll().stream().map(VendaRepositoryImplAdapter::cast).toList();
    }
}
