package br.com.fatec.venda.entity.enumerable;

/**
 * Ciclo de vida do pedido:
 * PENDENTE -> CONFIRMADA -> SAIU_PARA_ENTREGA -> ENTREGUE.
 * RECUSADA é um estado final fora do fluxo (venda barrada).
 */
public enum StatusVenda {
    PENDENTE,
    CONFIRMADA,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    RECUSADA;

    /** Próximo passo do fluxo normal, ou null se o status é final. */
    public StatusVenda proximo() {
        return switch (this) {
            case PENDENTE -> CONFIRMADA;
            case CONFIRMADA -> SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA -> ENTREGUE;
            case ENTREGUE, RECUSADA -> null;
        };
    }

    /** Só é permitido avançar exatamente um passo por vez. */
    public boolean podeAvancarPara(StatusVenda destino) {
        return proximo() == destino;
    }
}
