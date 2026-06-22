## 🎬 Room Booking

<br/> 

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat)](LICENSE)

<br/>

Room Booking é uma API REST para revervas de salas, desenvolvida com Spring Boot.


<br/>

**Arquitetura**

O projeto segue uma arquitetura em camadas (Layered Architecture), com testes de integração da web, testes de acesso a dados e testes de unidade das regras de negócio:

### Estrutura do Projeto

```text
src/
├── main/java/com/devsuperior/movieflix/
│   ├── config/                  # Configurações de segurança (OAuth2, Resource Server, CORS)
│   │   └── customgrant/         # Implementação do fluxo de autenticação customizado (password grant)
│   ├── controllers/             # Camada de apresentação (endpoints REST)
│   │   └── exceptions/          # Handler global de exceções
│   ├── dto/                     # Objetos de transferência de dados (Data Transfer Objects)
│   ├── entities/                # Entidades JPA (Domínio)
│   ├── projections/             # Projeções para queries nativas
│   ├── repositories/            # Camada de acesso a dados (Spring Data JPA)
│   └── services/                # Camada de regras de negócio
│       └── exceptions/          # Exceções de domínio
│
└── test/java/com/devsuperior/movieflix/
    ├── controllers/             # Testes de integração da camada Web (Endpoints e Handlers)
    ├── repositories/            # Testes de integração de dados (Consultas e Projections)
    └── services/                # Testes unitários isolados (Regras de negócio com Mocks)
```

<br/> 

**Fluxo de autenticação**

<br/>

A aplicação utiliza OAuth2 Authorization Server com um grant type customizado (password). O cliente envia credenciais via POST /oauth2/token e recebe um JWT, que deve ser incluído como Bearer Token no header Authorization das requisições subsequentes.
Controle de acesso

<br/>

🛠️ **Tecnologias utilizadas**

Java 21

Spring Boot 3.4.4

Spring Security (OAuth2 Authorization Server + Resource Server)

Spring Data JPA / Hibernate

H2 Database (banco em memória para perfil de testes)

Maven

<br/>

**📡 Endpoints por perfil de acesso**

**Legenda de códigos**


| Código | Significado |
|---|---|
| `200` | OK — requisição bem-sucedida |
| `201` | Created — recurso criado com sucesso |
| `401` | Unauthorized — token ausente ou inválido |
| `403` | Forbidden — autenticado, mas sem permissão |
| `404` | Not Found — recurso não encontrado |
| `422` | Unprocessable Entity — dados inválidos no body |

---

**🎭 Gêneros**

| Método | Endpoint | Perfil | Situação | Retorno |
|---|---|---|---|---|
| `GET` | `/genres` | VISITOR / MEMBER | Sem token | `401` |
| `GET` | `/genres` | VISITOR | Autenticado | `200` |
| `GET` | `/genres` | MEMBER | Autenticado | `200` |

---

**🎬 Filmes**

| Método | Endpoint | Perfil | Situação | Retorno |
|---|---|---|---|---|
| `GET` | `/movies/{id}` | VISITOR / MEMBER | Sem token | `401` |
| `GET` | `/movies/{id}` | VISITOR | Autenticado, ID existe | `200` |
| `GET` | `/movies/{id}` | MEMBER | Autenticado, ID existe | `200` |
| `GET` | `/movies/{id}` | VISITOR / MEMBER | ID inexistente | `404` |
| `GET` | `/movies` | VISITOR / MEMBER | Sem token | `401` |
| `GET` | `/movies` | VISITOR | Autenticado | `200` |
| `GET` | `/movies` | MEMBER | Autenticado | `200` |
| `GET` | `/movies?genreId={id}` | VISITOR / MEMBER | Autenticado com gênero válido | `200` |

---

**⭐ Reviews**

| Método | Endpoint | Perfil | Situação | Retorno |
|---|---|---|---|---|
| `POST` | `/reviews` | VISITOR / MEMBER | Sem token | `401` |
| `POST` | `/reviews` | VISITOR | Autenticado (sem permissão) | `403` |
| `POST` | `/reviews` | MEMBER | Autenticado, dados válidos | `201` |
| `POST` | `/reviews` | MEMBER | Autenticado, dados inválidos | `422` |


<br/>


**🚀 Como executar o projeto**

**📋 Pré-requisitos**

- Java 21 ou superior instalado

- Maven 3.8 ou superior instalado

- Git instalado.

- IDE que suporte Java


<br/>


**1. Clone o repositório**

git clone https://github.com/seu-usuario/movieflix.git
cd movieflix

**2. Execute com Maven**

./mvnw spring-boot:run

A aplicação sobe por padrão no perfil test (banco H2 em memória) na porta 8080.

**3. Acesse o console H2 (opcional)**

http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:testdb

User: sa

Password: (vazio)

**4. Usuários disponíveis para testes**

| Usuário | Senha | Perfil |
| :--- | :--- | :--- |
| ana@gmail.com | 123456 | **MEMBER** |
| bob@gmail.com | 123456 | **VISITOR** |

**🔑 Autenticação**
Antes de chamar os endpoints protegidos, obtenha um token de acesso:

Requisição:

POST /oauth2/token
Authorization: Basic bXljbGllb... 
Content-Type: application/x-www-form-urlencoded
grant_type=password&username=ana@gmail.com&password=..... 

O header Basic é o clientId:clientSecret 
(myclientid:myclientsecret) em Base64.

Resposta:

{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 86400
}

Use o access_token retornado no header das próximas requisições:

Authorization: Bearer {access_token}

📡 **Endpoints**

👤 Usuários

GET /users/profile

Retorna os dados do usuário autenticado.

Acesso: VISITOR ou MEMBER

Resposta 200 OK:


{
  "id": 2,
  "name": "Ana",
  "email": "ana@gmail.com"
}


🎭 Gêneros

GET /genres

Lista todos os gêneros disponíveis.

Acesso: VISITOR ou MEMBER

Resposta 200 OK:

  { "id": 1, "name": "Comédia" },
  { "id": 2, "name": "Terror" },
  { "id": 3, "name": "Drama" }

  Resposta 401 Unauthorized — quando não há token válido.

🎬 Filmes

GET /movies

Lista filmes paginados, ordenados por título. Aceita filtro opcional por gênero.

Acesso: VISITOR ou MEMBER

GET /movies/{id}

Retorna os detalhes completos de um filme, incluindo gênero.

Acesso: VISITOR ou MEMBER

Resposta 200 OK:

{
  "id": 1,
  "title": "A Voz do Silêncio",
  "subTitle": "A Silent Voice",
  "year": 2016,
  "imgUrl": "https://...",
  "synopsis": "Um garoto que intimidou...",
  "genre": {
    "id": 2,
    "name": "Terror"
  }
}

Resposta 401 Unauthorized — sem token.

Resposta 404 Not Found — ID inexistente:

{
  "timestamp": "2025-05-18T12:00:00Z",
  "status": 404,
  "error": "Resource not found",
  "message": "Recurso não encontrado",
  "path": "/movies/99999"
}


⭐ Reviews

POST /reviews

Publica uma review para um filme.

Acesso: somente MEMBER

Body:

{
  "text": "Filme incrível, recomendo muito!",
  "movieId": 1
}


Resposta 201 Created:


{
  "id": 1,
  "text": "Filme incrível, recomendo muito!",
  "movieId": 1,
  "userId": 2,
  "userName": "Ana",
  "userEmail": "ana@gmail.com"
}


Resposta 401 Unauthorized — sem token.

Resposta 403 Forbidden — usuário autenticado como VISITOR.

Resposta 422 Unprocessable Entity — dados inválidos (ex: texto em branco ou movieId nulo):


{
  "timestamp": "2025-05-18T12:00:00Z",
  "status": 422,
  "error": "Validation exception",
  "errors": 
    { "fieldName": "text", "message": "Campo requerido" }
  
}


**🧪 Testes**
O projeto contém testes de integração cobrindo os principais cenários de cada recurso.
Para executar os testes:

./mvnw test

Os testes são executados com banco H2 em memória e validam autenticação, autorização e regras de negócio de genres, movies e reviews.

<br/>

**⚙️ Variáveis de ambiente**

| Variável | Padrão | Descrição |
|---|---|---|
| `APP_PROFILE` | `test` | Perfil Spring ativo |
| `CLIENT_ID` | `myclientid` | Client ID do OAuth2 |
| `CLIENT_SECRET` | `myclientsecret` | Client Secret do OAuth2 |
| `JWT_DURATION` | `86400` | Duração do token em segundos |
| `CORS_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Origens permitidas pelo CORS |


<br/>


# Agradecimentos / Referências: 

<br/> 

DevSuperior - Escola de programação 

----------

# Autora:

Sheila M. M. L. Silva 

https://www.linkedin.com/in/sheilasheila/



