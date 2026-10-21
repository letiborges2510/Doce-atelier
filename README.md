# Doce Atelier

Sistema de e-commerce de uma confeitaria, feito com **microsserviços Spring Boot**, um **API Gateway** que centraliza toda a segurança e um **front-end React**. Projeto acadêmico da Fatec (Atividade 3).

O cliente navega pela vitrine, cria conta, compra e acompanha o pedido mudar de status sozinho. O administrador cadastra produtos, acompanha os pedidos e vê o relatório de faturamento.

---

## Sumário

1. [Visão geral da arquitetura](#1-visão-geral-da-arquitetura)
2. [Estrutura do repositório](#2-estrutura-do-repositório)
3. [Como cada parte funciona](#3-como-cada-parte-funciona)
4. [Pré-requisitos](#4-pré-requisitos)
5. [Passo a passo para rodar](#5-passo-a-passo-para-rodar)
6. [Como testar](#6-como-testar)
7. [Teste de concorrência (k6)](#7-teste-de-concorrência-k6)
8. [Problemas comuns](#8-problemas-comuns)

---

## 1. Visão geral da arquitetura

```
                    ┌──────────────┐
   Front React ───► │   gateway    │  :8080   autenticação + autorização
   (localhost:5173) │ (único acesso)│         (JWT, regras de acesso)
                    └──────┬───────┘
          ┌────────────┬───┴────────┬─────────────┐
          ▼            ▼            ▼             ▼
      ┌───────┐   ┌────────┐   ┌────────┐   ┌───────────┐
      │ login │   │ gestao │◄──│ venda  │──►│ relatorio │
      │ :8081 │   │ :8082  │REST│ :8083 │MQ │   :8084   │
      │MongoDB│   │  H2    │   │  H2    │   │    H2     │
      └───────┘   └────────┘   └────────┘   └───────────┘
                                    │  RabbitMQ (eventos) ▲
                                    └──────────────────────┘
```

| Serviço | Porta | Banco | Responsabilidade |
|---|---|---|---|
| `gateway` | 8080 | — | Porta de entrada única. Valida o JWT e aplica as regras de acesso. |
| `login` | 8081 | MongoDB | Cadastro, login (emite o JWT) e dados do usuário. |
| `gestao` | 8082 | H2 | Catálogo de produtos e estoque. |
| `venda` | 8083 | H2 | Pedidos, ciclo de vida do pedido e baixa de estoque. |
| `relatorio` | 8084 | H2 | Faturamento e quantidade de pedidos, a partir de eventos. |

**Como os serviços conversam**

- **Síncrono (REST):** o `venda` chama o `gestao` para baixar (ou devolver) estoque no momento da compra.
- **Assíncrono (RabbitMQ):** o `venda` publica eventos de pedido e o `gestao` publica eventos de produto. O `relatorio` consome os dois e mantém o seu próprio modelo de leitura, sem atrasar a compra.

**Padrão interno de cada serviço (hexagonal):** um módulo `domain` (entidades, portas e casos de uso, Java puro) e um módulo `springframework` (controllers, persistência, RabbitMQ).

---

## 2. Estrutura do repositório

```
Doce-atelier/
├── backend/
│   ├── gateway/      # Spring Cloud Gateway (WebMVC) + segurança
│   ├── login/        # usuários e JWT (MongoDB)
│   ├── gestao/       # produtos e estoque
│   ├── venda/        # pedidos
│   └── relatorio/    # dashboard
├── frontend/         # React + Vite + Tailwind
└── k6-tests/
    └── teste-concorrencia-multipla.js
```

---

## 3. Como cada parte funciona

### Segurança centralizada no gateway
Só o gateway tem segurança. Os outros serviços **não usam Spring Security** e confiam na identidade que o gateway repassa nos headers `X-User-Name` e `X-User-Roles` (qualquer `X-User-*` enviado pelo cliente é descartado).

- `gateway/.../configuration/RegrasDeAcesso.java`: lista de regras, da primeira para a última. A primeira que casa com método + caminho vale. O que não casa exige login.
- `gateway/.../configuration/GatewayAuthFilter.java`: lê o JWT (cookie `access_token` ou `Authorization: Bearer`), responde **401** (não logado) ou **403** (sem permissão).

| Nível | Quem acessa | Exemplos |
|---|---|---|
| Público | qualquer pessoa | cadastro, login, listar/ver produtos |
| Logado | cliente ou admin | fazer compra, ver os próprios pedidos |
| Admin | só administrador | criar/editar produto, listar todos os pedidos, avançar status, relatório |
| Bloqueado | só serviço com serviço | baixar e repor estoque |

### Pedidos (serviço `venda`)
O ciclo de vida do pedido é controlado pela API de vendas:

`PENDENTE → CONFIRMADA → SAIU_PARA_ENTREGA → ENTREGUE` (`RECUSADA` fica fora do fluxo).

- O pedido nasce `PENDENTE` e **avança sozinho** (um agendador verifica a cada 3 s e muda a etapa após 10 s; configurável em `pedido.automatico` no `application.yml` do `venda`).
- O admin também pode avançar na mão: `PATCH /fatec/venda/v1/venda/{id}/status` com `{ "status": "CONFIRMADA" }`. Só vale um passo de cada vez (pular etapa devolve **409**).
- Cada mudança de status é publicada como evento para o relatório.
- No front, a tela de pedidos consulta a API a cada 3 s.

### Relatório (serviço `relatorio`)
Guarda **uma linha por pedido**, atualizada a cada evento, sem duplicar nem voltar de status. Pedidos recusados não entram no faturamento. `GET /fatec/relatorio/v1/resumo` (admin) traz faturamento, pedidos, produtos ativos, clientes e `pedidosPorStatus`.

### Estoque e concorrência
- A baixa de estoque usa **lock pessimista** no `gestao`: duas compras do mesmo produto ao mesmo tempo são processadas uma de cada vez e nunca vendem mais que o estoque.
- Pedido com vários itens: se um item falha, o `venda` **devolve** ao estoque o que já tinha baixado (`POST /v1/produto/{id}/repor-estoque`, bloqueado no gateway).

---

## 4. Pré-requisitos

| Ferramenta | Versão | Para quê |
|---|---|---|
| Java (JDK) | 17 ou superior | backend |
| Maven | 3.9+ | compilar o backend |
| Node.js | 20 ou superior | front-end |
| MongoDB | 6+ | serviço `login` |
| RabbitMQ | qualquer 3.x | eventos entre serviços |
| k6 | 0.50+ (opcional) | teste de concorrência |
| Postman (opcional) | — | testar as APIs |

MongoDB e RabbitMQ podem rodar em Docker (veja o passo 2).

---

## 5. Passo a passo para rodar

### 5.1 Clonar
```bash
git clone https://github.com/letiborges2510/Doce-atelier.git
cd Doce-atelier
```

### 5.2 Subir MongoDB e RabbitMQ
Com Docker:
```bash
docker run -d --name mongo -p 27017:27017 mongo:7
docker run -d --name rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```
O MongoDB fica em `localhost:27017` (banco `fatec`, criado sozinho). O painel do RabbitMQ fica em http://localhost:15672 (usuário `guest`, senha `guest`).

Sem configurar nada, os serviços já conectam em `localhost:5672` com `guest/guest`.

**Usando um RabbitMQ na nuvem (ex.: CloudAMQP)?** Defina estas variáveis de ambiente antes de subir `gestao`, `venda` e `relatorio` (no IntelliJ: *Run > Edit Configurations > Environment variables*):

| Variável | Padrão | Exemplo CloudAMQP |
|---|---|---|
| `RABBITMQ_HOST` | `localhost` | `seu-host.rmq.cloudamqp.com` |
| `RABBITMQ_PORT` | `5672` | `5671` |
| `RABBITMQ_USER` | `guest` | seu usuário |
| `RABBITMQ_PASSWORD` | `guest` | sua senha |
| `RABBITMQ_VHOST` | `/` | seu vhost |
| `RABBITMQ_SSL` | `false` | `true` |

### 5.3 Subir o backend
Cada serviço é um projeto Maven. Abra um terminal por serviço, **nesta ordem**: `login`, `gestao`, `venda`, `relatorio`, `gateway`.

Serviços com dois módulos (`login`, `gestao`, `venda`, `relatorio`):
```bash
cd backend/login
mvn clean install -DskipTests
cd springframework
mvn spring-boot:run
```
Repita trocando `login` por `gestao`, `venda` e `relatorio`.

Gateway (módulo único):
```bash
cd backend/gateway
mvn spring-boot:run
```

Se preferir, abra a pasta `backend` no IntelliJ, deixe o Maven importar todos os `pom.xml` e rode a classe `Application` de cada serviço.

Confira se todos subiram: portas **8080, 8081, 8082, 8083 e 8084** livres e sem erro no console. Os bancos H2 são criados sozinhos na sua pasta de usuário (`~/gestao-db`, `~/venda-db`, `~/relatorio-db`).

### 5.4 Subir o front-end
```bash
cd frontend
cp .env.example .env      # no Windows: copy .env.example .env
npm install
npm run dev
```
Abra http://localhost:5173.

> **Importante:** o arquivo `.env` precisa existir, com `VITE_API_URL=http://localhost:8080`. Sem ele o site entra em **modo demonstração**: tudo fica só no navegador e nada chega ao backend nem ao MongoDB. Depois de criar ou mudar o `.env`, pare e rode `npm run dev` de novo.

---

## 6. Como testar

### 6.1 Pelo front
1. Abra http://localhost:5173 e veja a vitrine sem login.
2. Crie uma conta de **cliente**. Ela aparece no MongoDB (banco `fatec`, coleção `user`), com a senha em hash BCrypt.
3. Adicione produtos ao carrinho e finalize a compra.
4. Na tela de pedidos, veja o status mudar sozinho: *Aguardando confirmação → Confirmado → Em rota → Entregue*.
5. Para o painel admin, crie um usuário admin pela API (passo 6.2) e entre com ele.

### 6.2 Pelo Postman ou terminal
Todas as chamadas vão para o gateway (`http://localhost:8080`). Use `curl.exe` no PowerShell.

**1. Criar um admin** (já devolve o cookie de login):
```bash
curl -c admin.txt -X POST http://localhost:8080/fatec/login/v1/user/create \
  -H "Content-Type: application/json" \
  -d '{"nome":"Admin","username":"admin1","password":"Senha123!","email":"admin1@teste.com","cep":"","roles":["ADMIN"]}'
```

**2. Criar um produto** (admin):
```bash
curl -b admin.txt -X POST http://localhost:8080/fatec/gestao/v1/produto \
  -H "Content-Type: application/json" \
  -d '{"nome":"Brigadeiro","descricao":"Brigadeiro gourmet","categoria":"Docinhos","preco":5.5,"imagemUrl":"x","estoque":20,"ativo":true}'
```

**3. Listar produtos** (público):
```bash
curl http://localhost:8080/fatec/gestao/v1/produto
```

**4. Criar um cliente e comprar:**
```bash
curl -c cliente.txt -X POST http://localhost:8080/fatec/login/v1/user/create \
  -H "Content-Type: application/json" \
  -d '{"nome":"Cliente","username":"cliente1","password":"Senha123!","email":"cliente1@teste.com","cep":"","roles":["USER"]}'

curl -b cliente.txt -X POST http://localhost:8080/fatec/venda/v1/venda \
  -H "Content-Type: application/json" \
  -d '{"itens":[{"produtoId":"ID_DO_PRODUTO","quantidade":2}]}'
```

**5. Regras de acesso do gateway:**

| Chamada | Quem | Resultado esperado |
|---|---|---|
| `GET /fatec/gestao/v1/produto` | sem login | 200 |
| `POST /fatec/gestao/v1/produto` | sem login | 401 |
| `POST /fatec/gestao/v1/produto` | cliente | 403 |
| `POST /fatec/gestao/v1/produto` | admin | 201 |
| `POST /fatec/gestao/v1/produto/{id}/baixar-estoque` | qualquer um | 403 (rota interna) |
| `GET /fatec/venda/v1/venda` | cliente | 403 |
| `GET /fatec/venda/v1/venda?status=PENDENTE` | admin | 200 |
| `PATCH /fatec/venda/v1/venda/{id}/status` com `{"status":"CONFIRMADA"}` | admin | 200 (e 409 se pular etapa) |
| `GET /fatec/relatorio/v1/resumo` | admin | 200 |
| `GET /fatec/relatorio/v1/resumo` | cliente | 403 |

**6. Estoque insuficiente:** comprar mais que o estoque devolve **409** e nada é descontado.

---

## 7. Teste de concorrência (k6)

O arquivo `k6-tests/teste-concorrencia-multipla.js` roda **3 cenários ao mesmo tempo** pelo gateway, cada um com um usuário novo por compra:

| Cenário | O que faz | O que deve acontecer |
|---|---|---|
| A | 30 usuários disputam o mesmo produto (estoque 10) | exatamente 10 vendas |
| B | 3 produtos (estoques 5, 8 e 3), 15 compradores em cada | cada produto vende exatamente o seu estoque |
| C | 20 pedidos com 2 itens (C1 estoque 10, C2 estoque 4) | só 4 pedidos fecham; o C1 termina com 6 (o que falhou é devolvido) |

O script cria o admin e os produtos sozinho e, no final, confere se `estoque final = estoque inicial − vendido` e se nenhum estoque ficou negativo.

**Rodar:**
1. Instale o k6: https://grafana.com/docs/k6/latest/set-up/install-k6/
2. Com todo o backend no ar:
```bash
cd k6-tests
k6 run -e BASE_URL=http://localhost:8080 teste-concorrencia-multipla.js
```
3. Veja a tabela **CONFERÊNCIA FINAL** no fim. O esperado é `RESULTADO: PASSOU`.

Parâmetros opcionais: `-e QTD_A=30 -e ESTOQUE_A=10 -e QTD_B_POR_PRODUTO=15 -e QTD_C=20 -e ESTOQUE_C1=10 -e ESTOQUE_C2=4`.

Os `409` durante o teste são esperados (compra sem estoque). Só `erro_inesperado` indica problema, e o limite exige zero.

---

## 8. Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| Cadastro no front funciona mas o usuário não aparece no MongoDB | Falta o `frontend/.env` (modo demonstração). Crie com `VITE_API_URL=http://localhost:8080` e reinicie o `npm run dev`. |
| `Port 80xx was already in use` | Há uma instância antiga rodando. Pare-a antes de subir de novo. |
| `E11000 duplicate key ... username` | Esse usuário já existe no MongoDB. Use outro `username` ou apague o documento na coleção `user`. |
| Erro de H2 `Value not permitted for column ... CHECK` ao criar pedido | Banco H2 antigo com constraint de status. Pare o `venda` e apague a pasta `~/venda-db`. |
| `package org.springframework.amqp does not exist` | Maven desatualizado. Rode `mvn clean install` no serviço e recarregue o projeto na IDE. |
| Erro de conexão com o RabbitMQ ao subir | Confira se o RabbitMQ está no ar e as variáveis `RABBITMQ_*` (passo 5.2). |
| `401` sem corpo de resposta | Algum serviço ainda tem `spring-boot-starter-security` no `pom.xml`. Só o gateway tem segurança. |
| `Failed to set up a Bean Validation provider` no login | Aviso INFO sem efeito. |

---

## Tecnologias

Java 17, Spring Boot 4, Spring Cloud Gateway (WebMVC), Spring Data JPA (H2), Spring Data MongoDB, Spring AMQP (RabbitMQ), JWT (jjwt), BCrypt, React 19, Vite, Tailwind CSS, k6.
