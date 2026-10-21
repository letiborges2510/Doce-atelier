package br.com.fatec.venda.repository.orm;

import br.com.fatec.venda.entity.enumerable.StatusVenda;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaOrm implements Persistable<String> {

    @Id
    private String id;

    private String clienteId;

    @ElementCollection
    @CollectionTable(name = "venda_itens", joinColumns = @JoinColumn(name = "venda_id"))
    private List<ItemVendaOrm> itens;

    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    private StatusVenda status;

    private LocalDateTime dataHora;

    // Quando o status mudou pela última vez (a API usa isso para avançar o pedido)
    private LocalDateTime statusAtualizadoEm;

    @Override
    public boolean isNew() {
        return true;
    }
}