# 🛍️ Produto Service

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge" />
  <img src="https://img.shields.io/badge/RabbitMQ-Messaging-ff6600?style=for-the-badge" />
  <img src="https://img.shields.io/badge/PostgreSQL-Database-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Microservices-Architecture-black?style=for-the-badge" />
</p>

<p align="center">
  Microserviço responsável pelo gerenciamento de produtos em uma arquitetura orientada a eventos utilizando RabbitMQ, Spring Boot e PostgreSQL.
</p>

---

# 📖 Sobre o Projeto

O **Produto Service** é um microserviço desenvolvido com foco em arquitetura de microsserviços e comunicação assíncrona utilizando RabbitMQ.

O sistema é responsável por:

* Cadastro de produtos
* Atualização de status
* Comunicação entre microsserviços
* Processamento assíncrono de eventos
* Retry automático
* Dead Letter Queue (DLQ)
* Tratamento resiliente de falhas

---

# 🧠 Arquitetura

O projeto segue os princípios de:

* Event Driven Architecture (EDA)
* Separation of Concerns
* Resilience Patterns
* Retry Pattern
* Dead Letter Queue Pattern

---

# 🔄 Fluxo da Arquitetura

```text
Cliente/API
    ↓
Produto Service
    ↓
RabbitMQ Exchange
    ↓
Outros Microsserviços
    ↓
Microserviço de Estoque
    ↓
Evento de resposta
    ↓
Produto Service atualiza status do produto
```

---

# 🐇 Fluxo Retry + DLQ

```text
Mensagem recebida
        ↓
Consumer processa mensagem

SE DER ERRO:
        ↓
Retry automático em memória (3 tentativas síncronas via RetryOperationsInterceptor)

SE CONTINUAR FALHANDO:
        ↓
Mensagem é rejeitada (sem requeue) e cai na Dead Letter Queue (DLQ) via x-dead-letter-exchange
```

> O retry é feito em memória, dentro do próprio listener (`RetryOperationsInterceptor`, 3 tentativas síncronas antes de rejeitar). Não há uma retry queue intermediária com TTL — se uma mensagem esgota as tentativas, ela vai direto para a DLQ correspondente.

---

# 🔐 Segurança

O serviço valida tokens **JWT (HS256)** emitidos por um serviço de autenticação externo — este microserviço não faz login nem guarda usuário/senha, apenas valida a assinatura e extrai claims (`sub`, `roles`) do token recebido no header `Authorization: Bearer <token>`.

| Rota                     | Acesso                          |
| ------------------------ | -------------------------------- |
| `GET /produtos`, `GET /produtos/{id}` | Público                |
| `POST /produtos`, `PUT /produtos/{id}`, `DELETE /produtos/{id}` | Requer JWT válido |
| `/swagger-ui/**`, `/api-docs/**`, `/actuator/**` | Público          |

Sem token válido, as rotas protegidas retornam `401` no formato padrão de resposta da API.

## Configuração

```properties
app.security.jwt.secret=${JWT_SECRET}
```

O segredo precisa ser o **mesmo** configurado no serviço de autenticação que emite os tokens (HMAC compartilhado). Em `dev`/`test` há um valor padrão apenas para desenvolvimento local; em produção a variável `JWT_SECRET` é obrigatória. O valor default local é o mesmo usado por padrão no `auth-service` e no `api_gateway` (`my-super-secret-key-my-super-secret-key`), então os três funcionam juntos sem configuração extra em dev.

---

# 🧭 Service Discovery (Eureka)

O `produto-service` se registra num [Eureka Server](https://github.com/Helencb/eureka-server) para ser descoberto pelo [API Gateway](https://github.com/Helencb/api_gateway), que roteia `/api/product/**` para `lb://PRODUCT-SERVICE`.

```properties
eureka.client.service-url.defaultZone=${EUREKA_URL:http://admin:123456@localhost:8761/eureka/}
eureka.instance.appname=PRODUCT-SERVICE
```

* O nome de registro (`eureka.instance.appname`) é `PRODUCT-SERVICE` (inglês) para bater com a rota do gateway, mesmo com `spring.application.name=produto-service` (português) usado internamente em logs/métricas.
* O Eureka Server exige HTTP Basic em toda chamada (inclusive registro), por isso as credenciais (`admin:123456` por padrão) vêm embutidas na própria URL — sem isso o registro falha com 401 silenciosamente nos logs.
* Desabilitado no perfil `test` (`eureka.client.enabled=false`) para os testes não tentarem se conectar em nada.
* No perfil `docker`, vem desabilitado por padrão (`EUREKA_ENABLED=false`) porque o `docker-compose.yml` deste repositório não sobe eureka-server/gateway/auth-service — habilite com `EUREKA_ENABLED=true` se conectar este container na mesma rede dos outros serviços.

---

# 📤 Outbox Pattern

Os eventos de domínio (`ProdutoCriado`, `ProdutoAtualizado`, `ProdutoDesativado`) não são mais publicados diretamente no `RabbitTemplate` dentro da mesma chamada. Em vez disso:

```text
Produto salvo/atualizado no banco
        ↓
Evento gravado na tabela outbox_events (mesma transação do JPA)
        ↓
Transação é commitada
        ↓
Tentativa imediata de publicação no RabbitMQ (best-effort, fora da transação)

SE FALHAR (broker indisponível, etc.):
        ↓
Evento fica PENDENTE e o OutboxScheduler tenta novamente periodicamente
        ↓
Após esgotar app.outbox.max-tentativas → status FALHOU (requer investigação manual)
```

Isso resolve o problema de falta de garantia transacional: se o commit no banco falhar, o evento nunca é gravado na outbox (nada é publicado); se o commit for bem-sucedido mas o Rabbit estiver fora do ar no momento do publish, o evento fica registrado e é reconciliado automaticamente, sem se perder.

| Componente         | Responsabilidade                                                        |
| ------------------ | ------------------------------------------------------------------------ |
| `OutboxService`     | Grava o evento (status `PENDENTE`) na mesma transação do agregado        |
| `OutboxPublisher`   | Publica no Rabbit e atualiza o status (`PUBLICADO`/`PENDENTE`/`FALHOU`)   |
| `OutboxScheduler`   | Reconcilia periodicamente os eventos `PENDENTE` que ainda não foram publicados |

## Configuração

```properties
app.outbox.retry-delay-ms=30000   # intervalo do scheduler e backoff entre tentativas
app.outbox.auto-publish=true      # liga/desliga o OutboxScheduler
app.outbox.max-tentativas=5       # tentativas antes de marcar o evento como FALHOU
```

> A tabela `outbox_events` é criada automaticamente em `dev`/`test` (`ddl-auto=update`/`create-drop`). Em produção (`ddl-auto=validate`), a criação da tabela precisa ser aplicada manualmente ao banco antes do deploy, assim como já ocorre com a tabela `produtos`.

---

# 🚀 Tecnologias Utilizadas

## Back-End

* Java 21
* Spring Boot
* Spring Data JPA
* Spring AMQP
* Hibernate
* Maven
* Lombok

## Mensageria

* RabbitMQ

## Banco de Dados

* MySql

---

# 📂 Estrutura do Projeto

```bash
src/main/java
│
├── config
│   └── RabbitConfig
│
├── controller
│
├── dto
│
├── messaging
│   ├── consumer
│   ├── producer
│   ├── event
│   └── routing
│
├── model
│
├── repository
│
└── service
```

---

# 📦 Mensageria

## Exchanges

| Exchange               | Responsabilidade             |
| ---------------------- | ---------------------------- |
| `produto.exchange`     | Exchange principal           |
| `produto.dlq.exchange` | Exchange de mensagens mortas |

---

## Queues

| Queue                          | Responsabilidade           |
| ------------------------------ | -------------------------- |
| `produto.criacao.queue`        | Processamento de criação   |
| `produto.atualizacao.queue`    | Processamento de atualização |
| `produto.desativacao.queue`    | Processamento de desativação |
| `produto.catalogo.queue`       | Eventos de catálogo        |
| `produto.catalogo.dlq.queue`   | DLQ do catálogo            |
| `produto.dlq.queue`            | DLQ genérica                |

> `produto.criacao.queue`, `produto.atualizacao.queue` e `produto.desativacao.queue` hoje não têm consumer interno — elas existem para garantir que os eventos publicados no `produto.exchange` sejam roteados (o `RabbitTemplate` usa `mandatory=true`, então uma mensagem sem binding é apenas logada como "não roteada" e descartada). Outros microsserviços podem consumir dessas filas ou declarar as próprias.

---

## Routing Keys

| Routing Key           | Evento              |
| --------------------- | ------------------- |
| `produto.criado`      | Produto criado      |
| `produto.atualizado`  | Produto atualizado  |
| `produto.desativado`  | Produto desativado  |
| `produto.sem-estoque` | Produto sem estoque |
| `produto.em-estoque`  | Produto em estoque  |

---

# ✅ Funcionalidades

* Cadastro de produtos
* Atualização de status
* Comunicação assíncrona entre microsserviços
* Retry automático
* Dead Letter Queue (DLQ)
* Tratamento resiliente de falhas
* Logging estruturado
* Separação de responsabilidades
* Arquitetura orientada a eventos

---

# ⚙️ Configuração

## MySQL

```properties
spring.datasource.url=jdbc:mysql://localhost:5432/produto_db
spring.datasource.username=root
spring.datasource.password=root
```

---

## RabbitMQ

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

spring.rabbitmq.listener.simple.default-requeue-rejected=false
```

---

# 🐳 Docker / CI

## Subir o ambiente completo com Docker Compose

```bash
docker compose up --build
```

Isso sobe MySQL, RabbitMQ e o `produto-service` (perfil `docker`) já conectados entre si. A API fica disponível em `http://localhost:8080` e o RabbitMQ Management em `http://localhost:15672` (guest/guest).

## Build manual da imagem

```bash
docker build -t produto-service .
docker run -p 8080:8080 --env-file .env produto-service
```

## CI

O workflow `.github/workflows/ci.yml` roda em push/PR para `main`:

* Sobe um container de RabbitMQ como *service* do próprio job, permitindo que os testes de integração (`RabbitIntegrationTest`, `ProdutoServiceIntegrationTest`) rodem sem depender de infraestrutura local.
* Executa `./mvnw test`.
* Builda a imagem Docker como verificação adicional (`docker build`).

---

# ▶️ Como Executar

## 1. Clonar repositório

```bash
git clone https://github.com/seu-usuario/produto-service.git
```

---

## 2. Entrar na pasta

```bash
cd produto-service
```

---

## 3. Configurar MySQL

Criar database:

```sql
CREATE DATABASE produto_db;
```

---

## 4. Iniciar RabbitMQ

RabbitMQ Management:

```text
http://localhost:15672
```

Usuário padrão:

```text
guest
guest
```

---

## 5. Executar aplicação

```bash
./mvnw spring-boot:run
```

---

# 🧪 Testando Retry e DLQ

Para testar o fluxo de retry:

1. Force uma exception no consumer
2. Envie uma mensagem
3. Observe:

   * Retry automático
   * Mensagem sendo enviada para DLQ

---

# 📈 Conceitos Aplicados

* Event Driven Architecture
* Retry Pattern
* Dead Letter Queue
* Asynchronous Communication
* Resilience
* Separation of Responsibilities
* Clean Architecture Principles

---

# 🔮 Melhorias Futuras

* Testes unitários com JUnit e Mockito
* Testcontainers (os testes de integração ainda dependem de um RabbitMQ acessível, seja local ou via CI)
* Kubernetes
* API Gateway
* OpenFeign
* Prometheus + Grafana
* Observabilidade distribuída

---

# 👩‍💻 Desenvolvedora

## Helen Cristina

Back-End Developer • Java • Spring Boot • Microsservices • RabbitMQ

<p align="left">
  <a href="https://github.com/Helencb">
    <img src="https://img.shields.io/badge/GitHub-Perfil-black?style=for-the-badge&logo=github" />
  </a>
</p>
