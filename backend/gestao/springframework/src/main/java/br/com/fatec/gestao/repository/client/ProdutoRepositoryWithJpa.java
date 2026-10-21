package br.com.fatec.gestao.repository.client;

import br.com.fatec.gestao.repository.orm.ProdutoOrm;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoRepositoryWithJpa extends JpaRepository<ProdutoOrm, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProdutoOrm p where p.id = :id")
    Optional<ProdutoOrm> buscarComLockParaAtualizar(@Param("id") String id);

    long countByAtivoTrue();
}
