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

O segredo precisa ser o **mesmo** configurado no serviço de autenticação que emite os tokens (HMAC compartilhado). Em `dev`/`test` há um valor padrão apenas para desenvolvimento local; em produção a variável `JWT_SECRET` é obrigatória.

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
* Testcontainers
* Docker
* Kubernetes
* API Gateway
* OpenFeign
* Prometheus + Grafana
* CI/CD Pipeline
* Observabilidade distribuída
* Outbox pattern (as propriedades `app.outbox.*` ainda não têm implementação por trás)

---

# 👩‍💻 Desenvolvedora

## Helen Cristina

Back-End Developer • Java • Spring Boot • Microsservices • RabbitMQ

<p align="left">
  <a href="https://github.com/Helencb">
    <img src="https://img.shields.io/badge/GitHub-Perfil-black?style=for-the-badge&logo=github" />
  </a>
</p>
