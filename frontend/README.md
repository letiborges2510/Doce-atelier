# Doce Atelier — Front-end (React + Vite + Tailwind)

Visual idêntico ao wireframe do Figma Make, com camada de integração ao
backend real Spring Boot (gateway + microsserviços login/gestão/venda/relatório).

    npm install
    cp .env.example .env     # coloque a URL do API Gateway
    npm run dev

Sem `VITE_API_URL`, roda em modo demonstração (dados no navegador; admin: código `DOCE2025`).

Com `VITE_API_URL` apontando para o gateway (ex.: `http://localhost:8080`), o front
fala com o backend de verdade através de `src/api.ts`:

| Ação | Rota real |
|---|---|
| Login | `POST /fatec/login/v1/auth` `{ username, password }` |
| Cadastro | `POST /fatec/login/v1/user/create` `{ nome, username, password, email, cep, roles }` |
| Sessão válida? | `GET /fatec/login/v1/me` |
| Logout | `POST /fatec/login/v1/logout` |
| Listar produtos (público) | `GET /fatec/gestao/v1/produto` |
| Criar produto (admin) | `POST /fatec/gestao/v1/produto` |
| Editar produto (admin) | `PUT /fatec/gestao/v1/produto/{id}` |
| Ativar/ocultar produto (admin) | `PATCH /fatec/gestao/v1/produto/{id}/ativo` |
| Fazer um pedido | `POST /fatec/venda/v1/venda` `{ itens: [{ produtoId, quantidade }] }` |
| Meus pedidos | `GET /fatec/venda/v1/venda/minhas` |
| Todos os pedidos (admin) | `GET /fatec/venda/v1/venda` |
| Resumo do dashboard (admin) | `GET /fatec/relatorio/v1/resumo` |

### Pontos importantes dessa integração

- **Autenticação por cookie**, não por token no `localStorage`. O backend seta um
  cookie `access_token` HttpOnly no login/cadastro; todo `fetch` usa
  `credentials: 'include'`. Por isso o gateway precisa do CORS configurado com
  origem explícita (`http://localhost:5173`) e `Access-Control-Allow-Credentials: true`
  — nunca `*`, porque cookie com `*` não funciona.
- **E-mail usado como username**: o backend não tem um campo de e-mail separado
  para login, então o e-mail digitado no formulário é enviado como `username`.
- **Código de administrador (`DOCE2025`)** continua validado só no front, porque
  o backend real não tem essa checagem — qualquer um que já tenha acesso ao
  `POST /user/create` poderia se cadastrar como `ADMIN` de qualquer forma; o
  código é só uma barreira de uso para a demonstração, não uma trava de segurança.
- **Status do pedido**: o backend não tem fluxo Novo → Em preparo → Entregue, só
  uma venda já `CONFIRMADA`. No modo conectado ao backend, o status aparece
  como selo fixo ("Entregue"); o seletor editável só existe no modo demonstração.
- **Estoque obrigatório**: o backend exige `estoque` ao criar/editar um produto,
  então o formulário de produto no painel admin ganhou um campo "Estoque
  disponível".
