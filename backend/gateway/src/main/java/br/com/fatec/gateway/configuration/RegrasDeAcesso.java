package br.com.fatec.gateway.configuration;

import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * ============================================================
 *  CONTROLE DE ACESSO DO SISTEMA - TUDO É DEFINIDO AQUI
 * ============================================================
 * O API Gateway é o único responsável por segurança (ver
 * GatewayAuthFilter). Os microsserviços não têm Spring Security.
 *
 * Como ler: a lista é avaliada de cima para baixo e a PRIMEIRA regra
 * que casar com (método + caminho) vale. O que não casar com nenhuma
 * regra exige login (LOGADO).
 *
 *  Código HTTP quando o acesso é negado:
 *    401 -> não está logado        403 -> logado, mas sem permissão
 *
 * RESUMO
 * ------------------------------------------------------------
 *  PÚBLICO (sem login)
 *    - Cadastro e login de usuário
 *    - Ver a vitrine: listar produtos e ver um produto
 *  LOGADO (cliente ou admin)
 *    - Fazer uma compra
 *    - Ver as próprias compras e consultar um pedido (cliente só vê o seu)
 *    - Ver os próprios dados (/me)
 *  SOMENTE ADMIN
 *    - Criar, editar e ativar/desativar produto
 *    - Listar TODAS as vendas e avançar o status do pedido
 *    - Ver o relatório (faturamento, pedidos, etc.)
 *    - Rotas administrativas do login
 *  BLOQUEADO (só os microsserviços entre si)
 *    - Baixa e reposição de estoque (venda -> gestao)
 * ------------------------------------------------------------
 */
public final class RegrasDeAcesso {

    private RegrasDeAcesso() {
    }

    /** Uma regra: método HTTP ("*" = qualquer), caminho e quem pode acessar. */
    public record Regra(String metodo, String caminho, Acesso acesso) {
    }

    private static final String QUALQUER = "*";

    public static final List<Regra> REGRAS = List.of(

            // ================= LOGIN-SERVICE =================
            // --- público
            publico("POST", "/fatec/login/v1/auth"),             // fazer login
            publico("GET", "/fatec/login/v1/auth/forget/*"),     // esqueci a senha
            publico("POST", "/fatec/login/v1/user/create"),      // cadastro (já entra logado)
            publico("POST", "/fatec/login/v1/user/save"),        // cadastro simples
            publico("GET", "/fatec/login/v1/user/count"),        // total de clientes
            publico("POST", "/fatec/login/v1/logout"),           // sair
            // --- somente admin
            admin(QUALQUER, "/fatec/login/v1/admin/**"),
            // --- logado
            logado("GET", "/fatec/login/v1/me"),                 // meus dados

            // ================= GESTAO-SERVICE (produtos) =================
            // --- público: vitrine
            publico("GET", "/fatec/gestao/v1/produto"),          // listar produtos
            publico("GET", "/fatec/gestao/v1/produto/*"),        // ver um produto
            publico("GET", "/fatec/gestao/v1/produto/count-ativos"),
            // --- bloqueado: uso interno (o venda-service chama direto no gestao)
            bloqueado("POST", "/fatec/gestao/v1/produto/*/baixar-estoque"),
            bloqueado("POST", "/fatec/gestao/v1/produto/*/repor-estoque"),
            // --- somente admin: gerenciar produtos
            admin("POST", "/fatec/gestao/v1/produto"),           // criar produto
            admin("PUT", "/fatec/gestao/v1/produto/*"),          // editar produto
            admin("PATCH", "/fatec/gestao/v1/produto/*/ativo"),  // ativar/desativar produto
            admin("DELETE", "/fatec/gestao/v1/produto/*"),       // excluir produto
            admin(QUALQUER, "/fatec/gestao/**"),                 // qualquer outra rota do gestao

            // ================= VENDA-SERVICE (pedidos) =================
            // --- logado: cliente compra e vê as próprias compras
            logado("POST", "/fatec/venda/v1/venda"),             // fazer compra
            logado("GET", "/fatec/venda/v1/venda/minhas"),       // minhas compras
            logado("GET", "/fatec/venda/v1/venda/*"),            // consultar um pedido (a API só deixa ver o próprio)
            // --- somente admin: painel de pedidos
            admin("GET", "/fatec/venda/v1/venda"),               // todas as vendas (aceita ?status=)
            admin("PATCH", "/fatec/venda/v1/venda/*/status"),    // avançar status do pedido
            admin(QUALQUER, "/fatec/venda/**"),                  // qualquer outra rota do venda

            // ================= RELATORIO-SERVICE (dashboard) =================
            admin(QUALQUER, "/fatec/relatorio/**")               // tudo do relatório é só admin
    );

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /** Descobre quem pode acessar o método + caminho informados. */
    public static Acesso acessoExigido(String metodo, String caminho) {
        for (Regra regra : REGRAS) {
            boolean metodoOk = QUALQUER.equals(regra.metodo()) || regra.metodo().equalsIgnoreCase(metodo);
            if (metodoOk && MATCHER.match(regra.caminho(), caminho)) {
                return regra.acesso();
            }
        }
        return Acesso.LOGADO; // rota não prevista: exige login
    }

    private static Regra publico(String metodo, String caminho) {
        return new Regra(metodo, caminho, Acesso.PUBLICO);
    }

    private static Regra logado(String metodo, String caminho) {
        return new Regra(metodo, caminho, Acesso.LOGADO);
    }

    private static Regra admin(String metodo, String caminho) {
        return new Regra(metodo, caminho, Acesso.ADMIN);
    }

    private static Regra bloqueado(String metodo, String caminho) {
        return new Regra(metodo, caminho, Acesso.BLOQUEADO);
    }
}
