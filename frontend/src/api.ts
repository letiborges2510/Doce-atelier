// Camada de integração com o API Gateway (Spring). Com VITE_API_URL vazio, nada é chamado
// e o site funciona em modo demonstração, guardando os dados no navegador.
//
// Contrato real do backend (gateway em :8080):
//  - Auth é via cookie HttpOnly (access_token), setado pelo próprio backend no
//    login/cadastro. Por isso todo fetch usa credentials: 'include' e NUNCA
//    manda Authorization: Bearer - não existe token manipulável no front.
//  - Login usa {username, password} (não email/senha). Usamos o e-mail digitado
//    como username, já que o backend não tem um campo de e-mail separado pra
//    autenticação.
//  - Produto usa "imagemUrl" (não "imagem") e tem "estoque" obrigatório, mas
//    não tem "destaque".
//  - Venda tem status real no backend: PENDENTE, CONFIRMADA, SAIU_PARA_ENTREGA,
//    ENTREGUE (RECUSADA fica fora do fluxo). O admin avança um passo por vez
//    via PATCH /fatec/venda/v1/venda/{id}/status.
const BASE = (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, '')

type Role = 'admin' | 'customer'
type Product = { id: string; name: string; category: string; description: string; price: number; image: string; badge?: string; active: boolean; stock: number }
type Order = { id: string; customer: string; email: string; items: { name: string; quantity: number; price: number }[]; total: number; date: string; status: 'Aguardando confirmação' | 'Confirmado' | 'Em rota' | 'Entregue' }
type Resumo = { faturamentoTotal: number; pedidosRecebidos: number; produtosAtivos: number; clientesCadastrados: number }

async function http<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const res = await fetch(BASE + path, {
    method,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  if (!res.ok) throw new Error(String(res.status))
  return res.status === 204 ? (null as T) : res.json()
}
const safe = async <T,>(fn: () => Promise<T>): Promise<T | null> => { if (!BASE) return null; try { return await fn() } catch { return null } }

const toProduct = (p: any): Product => ({ id: String(p.id), name: p.nome, category: p.categoria, description: p.descricao, price: Number(p.preco), image: p.imagemUrl, active: p.ativo ?? true, stock: Number(p.estoque ?? 0) })
const STATUS_LABEL: Record<string, Order['status']> = {
  PENDENTE: 'Aguardando confirmação',
  CONFIRMADA: 'Confirmado',
  SAIU_PARA_ENTREGA: 'Em rota',
  ENTREGUE: 'Entregue',
}
const toOrder = (v: any): Order => ({
  id: String(v.id),
  customer: v.clienteId,
  email: v.clienteId,
  items: (v.itens ?? []).map((i: any) => ({ name: i.nomeProduto, quantity: i.quantidade, price: Number(i.precoUnitario) })),
  total: Number(v.valorTotal),
  date: v.dataHora,
  status: STATUS_LABEL[v.status] ?? 'Aguardando confirmação',
})

export const api = {
  enabled: !!BASE,

  async login(email: string, password: string) { // POST /fatec/login/v1/auth -> AuthClaimsResponse
    const r = await http<any>('/fatec/login/v1/auth', 'POST', { username: email, password })
    return { name: r.nome as string, role: (r.roles ?? []).includes('ADMIN') ? ('admin' as Role) : ('customer' as Role) }
  },

  async register(d: { name: string; email: string; password: string; role: Role; code: string }) { // POST /fatec/login/v1/user/create
    if (d.role === 'admin' && d.code !== 'DOCE2025') {
      // O backend real não valida um "código de administrador" - esse
      // gate fica só no front mesmo, pra não deixar qualquer cadastro
      // se auto-promover a ADMIN sem nenhuma checagem.
      throw new Error('403')
    }
    const r = await http<any>('/fatec/login/v1/user/create', 'POST', {
      nome: d.name,
      username: d.email,
      password: d.password,
      email: d.email,
      cep: '',
      roles: [d.role === 'admin' ? 'ADMIN' : 'USER'],
    })
    return { name: r.nome as string, role: (r.roles ?? []).includes('ADMIN') ? ('admin' as Role) : ('customer' as Role) }
  },

  async logout() { await safe(() => http('/fatec/login/v1/logout', 'POST')) },

  // Confere se já existe uma sessão válida (cookie) ao recarregar a página.
  async me(): Promise<{ name: string; email: string; role: Role } | null> {
    if (!BASE) return null
    try {
      const r = await http<any>('/fatec/login/v1/me')
      return { name: r.nome as string, email: r.username as string, role: (r.roles ?? []).includes('ADMIN') ? 'admin' : 'customer' }
    } catch { return null }
  },

  // Carrega o catálogo (público) e, se logado, os pedidos certos pro papel do
  // usuário: admin vê todas as vendas, cliente vê só as próprias.
  async carregar(role?: Role): Promise<{ products: Product[]; orders: Order[] } | null> {
    if (!BASE) return null
    try {
      const produtos = await http<any[]>('/fatec/gestao/v1/produto')
      let vendas: any[] = []
      if (role === 'admin') {
        vendas = await http<any[]>('/fatec/venda/v1/venda').catch((e) => {
          console.error('Falha ao carregar /fatec/venda/v1/venda (lista de todas as vendas, admin):', e)
          return []
        })
      } else if (role === 'customer') {
        vendas = await http<any[]>('/fatec/venda/v1/venda/minhas').catch((e) => {
          console.error('Falha ao carregar /fatec/venda/v1/venda/minhas:', e)
          return []
        })
      }
      return { products: produtos.map(toProduct), orders: vendas.map(toOrder) }
    } catch { return null }
  },

  // Só os pedidos (admin: todos; cliente: os próprios). Usado para acompanhar o
  // status, que quem controla é a API de vendas.
  async pedidos(role: Role): Promise<Order[] | null> {
    return safe(async () => {
      const vendas = await http<any[]>(role === 'admin' ? '/fatec/venda/v1/venda' : '/fatec/venda/v1/venda/minhas')
      return vendas.map(toOrder)
    })
  },

  async resumo(): Promise<Resumo | null> {
    return safe(() => http<Resumo>('/fatec/relatorio/v1/resumo'))
  },

  errorMessage: (e: unknown) => (e instanceof Error && e.message === '401' ? 'E-mail ou senha incorretos. Tente novamente.' : e instanceof Error && e.message === '403' ? 'Código de acesso administrativo incorreto.' : e instanceof Error && (e.message === '409' || e.message === '400') ? 'Este e-mail já está cadastrado. Faça login.' : 'Não conseguimos conectar agora. Tente novamente em instantes.'),

  // Faz a venda de verdade no backend (desconta estoque etc) e devolve o
  // pedido já com os valores calculados no servidor.
  async criarPedido(itens: { produtoId: string; quantidade: number }[]): Promise<Order | null> {
    const venda = await safe(() => http<any>('/fatec/venda/v1/venda', 'POST', { itens }))
    return venda ? toOrder(venda) : null
  },

  // Avança o status no backend (só ADMIN; um passo por vez). Para clientes
  // comuns devolve null (403) e a transição segue só visual no front.
  status: (id: string, status: Order['status']) => {
    const codigo = Object.entries(STATUS_LABEL).find(([, label]) => label === status)?.[0]
    return codigo ? safe(() => http(`/fatec/venda/v1/venda/${id}/status`, 'PATCH', { status: codigo })) : Promise.resolve(null)
  },

  // Cria (sem id) ou atualiza (com id) um produto, devolvendo o produto já
  // persistido - importante pra criação, já que o id de verdade vem do
  // servidor (UUID), não pode ser inventado no front.
  async salvarProduto(p: { name: string; category: string; description: string; price: number; image: string; stock: number }, id?: string): Promise<Product | null> {
    const body = { nome: p.name, descricao: p.description, categoria: p.category, preco: p.price, imagemUrl: p.image, estoque: p.stock, ativo: true }
    const produto = await safe(() => http<any>(id ? `/fatec/gestao/v1/produto/${id}` : '/fatec/gestao/v1/produto', id ? 'PUT' : 'POST', body))
    return produto ? toProduct(produto) : null
  },

  visibilidade: (id: string, ativo: boolean) => safe(() => http(`/fatec/gestao/v1/produto/${id}/ativo`, 'PATCH', { ativo })),
}
