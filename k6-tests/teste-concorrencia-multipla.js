// teste-concorrencia-multipla.js — Doce Atelier
//
// Concorrência de MÚLTIPLOS cenários ao mesmo tempo, todos pelo gateway:
//
//   A) vários USUÁRIOS diferentes comprando o MESMO produto
//      (30 compradores disputando 10 unidades -> exatamente 10 vendas)
//   B) vários PRODUTOS ao mesmo tempo, cada um com seu estoque
//      (3 produtos, 15 compradores em cada -> cada produto vende exatamente o seu estoque)
//   C) pedidos com VÁRIOS ITENS ao mesmo tempo
//      (cada pedido leva 1 un. de C1 e 1 un. de C2; C2 tem menos estoque que C1.
//       Os pedidos que falham no 2º item precisam DEVOLVER o 1º item ao estoque)
//
// O próprio script cria o admin e os produtos (não precisa cadastrar nada antes)
// e, no final, confere no banco se o estoque bate com as vendas confirmadas:
//   estoque final = estoque inicial - unidades vendidas (e nunca negativo).
//
// Uso:
//   k6 run -e BASE_URL=http://localhost:8080 teste-concorrencia-multipla.js
//
// Parâmetros opcionais (-e NOME=valor):
//   QTD_A=30 ESTOQUE_A=10 | QTD_B_POR_PRODUTO=15 | QTD_C=20 ESTOQUE_C1=10 ESTOQUE_C2=4

import http from 'k6/http';
import execution from 'k6/execution';
import { Counter } from 'k6/metrics';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const SENHA = 'Senha123!k6';

const QTD_A = Number(__ENV.QTD_A || 30);
const ESTOQUE_A = Number(__ENV.ESTOQUE_A || 10);
const ESTOQUES_B = [5, 8, 3];
const QTD_B_POR_PRODUTO = Number(__ENV.QTD_B_POR_PRODUTO || 15);
const QTD_C = Number(__ENV.QTD_C || 20);
const ESTOQUE_C1 = Number(__ENV.ESTOQUE_C1 || 10);
const ESTOQUE_C2 = Number(__ENV.ESTOQUE_C2 || 4);

const vendasConfirmadas = new Counter('vendas_confirmadas'); // 201
const vendasRecusadas = new Counter('vendas_recusadas');     // 409 (sem estoque)
const erroInesperado = new Counter('erro_inesperado');       // qualquer outro status

export const options = {
    scenarios: {
        a_usuarios_mesmo_produto: {
            executor: 'shared-iterations', vus: QTD_A, iterations: QTD_A,
            maxDuration: '120s', exec: 'cenarioA',
        },
        b_varios_produtos: {
            executor: 'shared-iterations',
            vus: QTD_B_POR_PRODUTO * ESTOQUES_B.length, iterations: QTD_B_POR_PRODUTO * ESTOQUES_B.length,
            maxDuration: '120s', exec: 'cenarioB',
        },
        c_pedido_varios_itens: {
            executor: 'shared-iterations', vus: QTD_C, iterations: QTD_C,
            maxDuration: '120s', exec: 'cenarioC',
        },
    },
    thresholds: {
        erro_inesperado: ['count==0'],
    },
};

const json = { headers: { 'Content-Type': 'application/json' } };

function cadastrarUsuario(username, role) {
    return http.post(`${BASE_URL}/fatec/login/v1/user/create`, JSON.stringify({
        nome: `K6 ${username}`, username, password: SENHA,
        email: `${username}@k6.teste`, cep: '', roles: [role],
    }), json);
}

function criarProduto(nome, estoque) {
    const res = http.post(`${BASE_URL}/fatec/gestao/v1/produto`, JSON.stringify({
        nome, descricao: 'produto de teste de concorrência', categoria: 'Teste',
        preco: 10, imagemUrl: 'x', estoque, ativo: true,
    }), json);
    if (res.status !== 201) {
        throw new Error(`Falha ao criar produto "${nome}" (status ${res.status}): ${res.body}`);
    }
    return JSON.parse(res.body).id;
}

// ---------------------------------------------------------------------
// setup(): cria o admin e os produtos do teste
// ---------------------------------------------------------------------
export function setup() {
    const run = Date.now();
    const admin = `k6m-${run}-admin`;

    const resAdmin = cadastrarUsuario(admin, 'ADMIN'); // já devolve o cookie de login
    if (resAdmin.status !== 201) {
        throw new Error(`Falha ao criar o admin de teste (status ${resAdmin.status}): ${resAdmin.body}`);
    }

    const dados = {
        run,
        admin,
        a: criarProduto(`K6-${run}-A`, ESTOQUE_A),
        b: ESTOQUES_B.map((e, i) => criarProduto(`K6-${run}-B${i + 1}`, e)),
        c1: criarProduto(`K6-${run}-C1`, ESTOQUE_C1),
        c2: criarProduto(`K6-${run}-C2`, ESTOQUE_C2),
    };

    console.log('======================================================');
    console.log('Produtos criados para o teste:');
    console.log(`  A  (estoque ${ESTOQUE_A}): ${dados.a}`);
    ESTOQUES_B.forEach((e, i) => console.log(`  B${i + 1} (estoque ${e}): ${dados.b[i]}`));
    console.log(`  C1 (estoque ${ESTOQUE_C1}): ${dados.c1}`);
    console.log(`  C2 (estoque ${ESTOQUE_C2}): ${dados.c2}`);
    console.log('Disparando os 3 cenários ao mesmo tempo...');
    console.log('======================================================');
    return dados;
}

// ---------------------------------------------------------------------
// Cada iteração = um USUÁRIO novo (cadastro já loga) fazendo 1 pedido
// ---------------------------------------------------------------------
function pedido(data, tag, itens) {
    const username = `k6m-${data.run}-${tag}-${__VU}-${__ITER}`;
    const resUser = cadastrarUsuario(username, 'USER');
    if (resUser.status !== 201) {
        erroInesperado.add(1);
        console.error(`cadastro do usuário ${username} falhou: ${resUser.status} ${resUser.body}`);
        return;
    }

    const res = http.post(`${BASE_URL}/fatec/venda/v1/venda`, JSON.stringify({ itens }), json);
    if (res.status === 201) {
        vendasConfirmadas.add(1);
    } else if (res.status === 409) {
        vendasRecusadas.add(1);
    } else {
        erroInesperado.add(1);
        console.error(`venda inesperada (${tag}): ${res.status} ${res.body}`);
    }
}

export function cenarioA(data) {
    pedido(data, 'a', [{ produtoId: data.a, quantidade: 1 }]);
}

export function cenarioB(data) {
    const i = execution.scenario.iterationInTest % ESTOQUES_B.length;
    pedido(data, 'b', [{ produtoId: data.b[i], quantidade: 1 }]);
}

export function cenarioC(data) {
    pedido(data, 'c', [
        { produtoId: data.c1, quantidade: 1 },
        { produtoId: data.c2, quantidade: 1 },
    ]);
}

// ---------------------------------------------------------------------
// teardown(): confere se o estoque bate com o que foi vendido
// ---------------------------------------------------------------------
export function teardown(data) {
    const resLogin = http.post(`${BASE_URL}/fatec/login/v1/auth`,
        JSON.stringify({ username: data.admin, password: SENHA }), json);
    if (resLogin.status !== 200) {
        throw new Error(`Não consegui logar como admin para conferir (status ${resLogin.status})`);
    }

    const resVendas = http.get(`${BASE_URL}/fatec/venda/v1/venda`);
    if (resVendas.status !== 200) {
        throw new Error(`Não consegui listar as vendas (status ${resVendas.status}): ${resVendas.body}`);
    }

    // unidades vendidas (pedidos gravados) por produto, só das vendas desta execução
    const vendido = {};
    JSON.parse(resVendas.body)
        .filter((v) => String(v.clienteId).startsWith(`k6m-${data.run}-`))
        .forEach((v) => v.itens.forEach((item) => {
            vendido[item.produtoId] = (vendido[item.produtoId] || 0) + item.quantidade;
        }));

    const estoqueFinal = (id) => JSON.parse(http.get(`${BASE_URL}/fatec/gestao/v1/produto/${id}`).body).estoque;

    const casos = [
        { nome: 'A  (30 usuários, 1 produto)', id: data.a, inicial: ESTOQUE_A, esperadoVendido: Math.min(ESTOQUE_A, QTD_A) },
        ...ESTOQUES_B.map((e, i) => ({
            nome: `B${i + 1} (vários produtos)`, id: data.b[i], inicial: e, esperadoVendido: Math.min(e, QTD_B_POR_PRODUTO),
        })),
        { nome: 'C1 (pedido com 2 itens)', id: data.c1, inicial: ESTOQUE_C1, esperadoVendido: Math.min(ESTOQUE_C1, ESTOQUE_C2, QTD_C) },
        { nome: 'C2 (pedido com 2 itens)', id: data.c2, inicial: ESTOQUE_C2, esperadoVendido: Math.min(ESTOQUE_C1, ESTOQUE_C2, QTD_C) },
    ];

    const falhas = [];
    console.log('======================================================');
    console.log('CONFERÊNCIA FINAL');
    console.log('produto                          inicial vendido final  situação');
    casos.forEach((c) => {
        const v = vendido[c.id] || 0;
        const final = estoqueFinal(c.id);
        const problemas = [];
        if (final < 0) problemas.push('estoque NEGATIVO');
        if (final !== c.inicial - v) problemas.push(`estoque (${final}) != inicial - vendido (${c.inicial - v}) -> estoque vazou`);
        if (v !== c.esperadoVendido) problemas.push(`vendido ${v}, esperado ${c.esperadoVendido}`);
        const ok = problemas.length === 0;
        console.log(`${c.nome.padEnd(32)} ${String(c.inicial).padStart(5)} ${String(v).padStart(7)} ${String(final).padStart(5)}  ${ok ? 'OK' : 'FALHOU: ' + problemas.join('; ')}`);
        if (!ok) falhas.push(`${c.nome}: ${problemas.join('; ')}`);
    });
    console.log('======================================================');
    console.log(falhas.length === 0
        ? 'RESULTADO: PASSOU - nenhuma venda a mais, nenhum estoque negativo, nenhum item "meio vendido".'
        : 'RESULTADO: FALHOU');
    if (falhas.length > 0) {
        throw new Error(`Inconsistências encontradas:\n - ${falhas.join('\n - ')}`);
    }
}
