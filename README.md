## 🎬 Room Booking

<br/> 

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat)](LICENSE)

<br/>

Room Booking é uma API REST para revervas de salas, com containerização usando Docker desenvolvida com Spring Boot. Ela permite cadastrar salas e usuários, realizar reservas com validação de conflito de horários, reagendar e cancelar reservas, tudo com paginação e tratamento de erros centralizados. 


<br/>

**Sobre a API**

O Room Booking API é um sistema de reserva de salas que oferece controle completo sobre três entidades principais: 

**Entidades**

| Entidade | Descrição |
|-----------|-----------|
| **Room (Sala)** | Salas disponíveis para reserva, com **nome único**, capacidade e status **ativo/inativo**. O delete é um **soft delete** (desativação). |
| **User (Usuário)** | Usuários que realizam reservas. **E-mail único**, normalizado para **lowercase**. |
| **Booking (Reserva)** | Vínculo entre sala e usuário com período de tempo. Possui status **`ACTIVE`** ou **`CANCELLED`**. Nunca é deletada fisicamente. |


**🛠️ Tecnologias**

| Camada | Tecnologia |
|---------|------------|
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot 4.0.6 |
| **Persistência** | Spring Data JPA + Hibernate |
| **Banco de Dados** | MySQL 8.4 |
| **Migrations** | Flyway |
| **Validação** | Jakarta Bean Validation |
| **Documentação** | SpringDoc OpenAPI (Swagger UI) |
| **Boilerplate** | Lombok |
| **Testes** | JUnit 5 + Mockito + AssertJ |
| **Containerização** | Docker + Docker Compose |
| **Build** | Maven |



**Arquitetura**

O projeto segue uma arquitetura em camadas clássica do Spring Boot, com separação clara de responsabilidades. 


```text
                Controller
                     │
        ┌────────────┴────────────┐
        │                         │
      DTOs                    Service
(Request/Response)              │
                                ▼
                        Domain Entities
                  (Room, User, Booking)
                                │
                                ▼
                           Repository
                                │
                                ▼
                              MySQL
```


```text

````
**Controllers**
Recebem e validam as requisições HTTP, delegando a lógica ao Service 

**Services** concentram todas as regras de negócio e orquestram as operações

**Respositories** (JPA) Encapsulam o acesso ao banco de dados, incluindo queries customizadas com JPQL.

**Domain Entities** Carregam invariantes de domínio via factory methods e assertions internas

**GlobalExcaptionHandler** (@RestControllerAdvice) captura e forma todos os erros em um padrão único (ApiError)

**🔗 Endpoints**

**Endpoints - Rooms**

| Método | Rota | Descrição |
|--------|------|-----------|
| **GET** | `/api/v1/rooms` | Lista todas as salas (paginado). |
| **GET** | `/api/v1/rooms/{id}` | Busca uma sala pelo ID. |
| **POST** | `/api/v1/rooms` | Cria uma nova sala. |
| **PUT** | `/api/v1/rooms/{id}` | Atualiza os dados de uma sala. |
| **DELETE** | `/api/v1/rooms/{id}` | Desativa a sala (**soft delete**). |

**Usuários — /api/v1/users**

| Método | Rota | Descrição |
|--------|------|-----------|
| **GET** | `/api/v1/users` | Lista todos os usuários (paginado). |
| **GET** | `/api/v1/users/{id}` | Busca um usuário pelo ID. |
| **POST** | `/api/v1/users` | Cria um novo usuário. |
| **PUT** | `/api/v1/users/{id}` | Atualiza os dados de um usuário. |
| **DELETE** | `/api/v1/users/{id}` | Remove o usuário. |

**Reservas — /api/v1/bookings**

| Método | Rota | Descrição |
|--------|------|-----------|
| **GET** | `/api/v1/bookings` | Lista todas as reservas (paginado). |
| **GET** | `/api/v1/bookings/{id}` | Busca uma reserva pelo ID. |
| **GET** | `/api/v1/bookings/room/{roomId}` | Lista as reservas de uma sala (paginado). |
| **GET** | `/api/v1/bookings/user/{userId}` | Lista as reservas de um usuário (paginado). |
| **GET** | `/api/v1/bookings/room/{roomId}/period?from=...&to=...` | Lista as reservas de uma sala em um período. |
| **POST** | `/api/v1/bookings` | Cria uma nova reserva. |
| **PUT** | `/api/v1/bookings/{id}` | Reagenda uma reserva existente. |
| **DELETE** | `/api/v1/bookings/{id}` | Cancela uma reserva (status → **`CANCELLED`**). |

**Exemplo de corpo para criar uma reserva**

{
  "roomId": 1,
  "userId": 2,
  "startTime": "2026-07-10T09:00:00",
  "endTime": "2026-07-10T10:30:00"
}

**Padrão de erro retornado**

{
  "status": 409,
  "error": "Booking Conflict",
  "message": "Room already has an active booking from 2026-07-10T09:00 to 2026-07-10T10:30 (booking id: 5).",
  "timestamp": "2026-07-10T08:45:00"
}



**📁 Estrutura do Projeto**

src/
├── main/
│   ├── java/com/roombooking/
│   │   ├── controller/          # BookingController, RoomController, UserController
│   │   ├── service/             # BookingService, RoomService, UserService
│   │   ├── repository/          # BookingRepository, RoomRepository, UserRepository
│   │   ├── domain/
│   │   │   ├── entity/          # Booking, Room, User
│   │   │   └── enums/           # BookingStatus
│   │   ├── dto/
│   │   │   ├── request/         # BookingRequest, RoomRequest, UserRequest
│   │   │   └── response/        # BookingResponse, RoomResponse, UserResponse
│   │   └── exception/           # BusinessException, ConflictException, ResourceNotFoundException
│   │       └── handler/         # GlobalExceptionHandler, ApiError
│   └── resources/
│       ├── db/migration/        # V1__create_initial_schema.sql, V2__seed_data.sql
│       └── application.properties
└── test/
    └── java/com/roombooking/
        ├── domain/entity/       # BookingTest, RoomTest, UserTest
        └── service/             # BookingServiceTest, RoomServiceTest, UserServiceTest

        

**📐 Regras de Negócio**

Uma sala só aceita reservas se estiver com status active = true

Dois intervalos [a, b) e [c, d) conflitam quando a < d AND c < b — bookings adjacentes não conflitam

O status CANCELLED exclui a reserva de qualquer verificação de conflito futura

Ao reagendar, a própria reserva é excluída da checagem para não conflitar com seu horário anterior

O lock pessimista (PESSIMISTIC_WRITE) na query de conflito garante atomicidade em cenários de alta concorrência

E-mails de usuários são sempre normalizados para lowercase e sem espaços antes de serem persistidos

Nomes de salas são únicos (case-sensitive após trim)

endTime deve ser estritamente posterior ao startTime

**🧪 Testes Unitários**

Os testes cobrem as camadas de domínio e serviço, utilizando JUnit 5, Mockito e AssertJ.

## 🧪 Cobertura dos Testes

| Classe testada | Casos cobertos |
|----------------|----------------|
| **BookingTest** | Criação com campos corretos; datas inválidas (null, invertidas e iguais); sala inativa; cancelamento duplo; reagendamento com datas inválidas; `assertActive` em status `CANCELLED`. |
| **BookingServiceTest** | Criação sem conflito; criação com conflito (`ConflictException`); sala/usuário inexistente; sala inativa; bordas de intervalo (adjacentes e reservas canceladas); reagendamento excluindo o próprio booking; reagendamento com conflito; cancelamento duplo; listagem por sala/usuário com entidade inexistente. |
| **RoomTest** | Criação com campos corretos; trim do nome; capacidade zero/negativa; atualização de campos; soft delete; `assertActive` em sala inativa. |
| **RoomServiceTest** | Criação com nome único; nome duplicado; atualização sem conflito; atualização com nome de outra sala; sala não encontrada; manutenção do status quando `active` é `null`; soft delete; `findById`. |
| **UserTest** | Normalização de nome e e-mail na criação; conversão para lowercase; trim; atualização com normalização. |
| **UserServiceTest** | Criação com e-mail único; normalização do e-mail antes da checagem; e-mail duplicado; atualização com e-mail disponível; e-mail de outro usuário; usuário não encontrado; delete; `findById`. |

**Executar os testes**

bash
mvn test


**📄 Documentação Swagger**

A documentação interativa da API é gerada automaticamente pelo SpringDoc OpenAPI e está disponível após subir a aplicação:

http://localhost:8080/swagger-ui.html

Pelo Swagger UI é possível visualizar todos os endpoints, seus parâmetros, schemas de request/response e testar as chamadas diretamente no navegador.

🐳 Como executar com Docker

A aplicação é totalmente containerizada com Docker Compose, que sobe dois serviços:


mysql — MySQL 8.4 com volume persistente e healthcheck

api — A aplicação Spring Boot, que aguarda o MySQL estar saudável antes de iniciar (depends_on: condition: service_healthy)

Pré-requisitos


Docker e Docker Compose instalados

Passo a passo

**1. Clone o repositório**

git clone https://github.com/seu-usuario/room-booking.git

cd room-booking

**2. Configure as variáveis de ambiente**

Crie um arquivo .env na raiz do projeto (ele já está no .gitignore):

MYSQL_ROOT_PASSWORD=troqueEstaSenha123
MYSQL_DATABASE=roombooking

DB_URL=jdbc:mysql://mysql:3306/roombooking?createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=troqueEstaSenha123

⚠️ Importante: Altere as senhas antes de usar em qualquer ambiente que não seja local.


**3. Suba os containers**

bash

docker compose up --build

A aplicação estará disponível em http://localhost:8080 assim que o MySQL passar no healthcheck e as migrations do Flyway forem aplicadas.

**4. Parar os containers**

bash

docker compose down


**5.Para também remover o volume do banco de dados:**

**Como a imagem Docker é construída**

bash

docker compose down -v


**O Dockerfile** utiliza **multi-stage build** para manter a imagem final leve:

| Estágio | Base | O que faz |
|---------|------|-----------|
| **build** | `maven:3.9-eclipse-temurin-21` | Baixa as dependências do projeto e compila o arquivo `.jar` (sem executar os testes). |
| **runtime** | `eclipse-temurin:21-jre-alpine` | Copia apenas o arquivo `.jar` gerado no estágio anterior e define o *entrypoint* da aplicação. |

Isso garante que ferramentas de build (Maven, JDK completo) não entrem na imagem de produção, reduzindo significativamente seu tamanho.


**Variáveis de Ambiente** 

| Variável | Descrição | Exemplo |
|-----------|-----------|----------|
| `MYSQL_ROOT_PASSWORD` | Senha do usuário **root** do MySQL. | `troqueEstaSenha123` |
| `MYSQL_DATABASE` | Nome do banco de dados criado automaticamente. | `roombooking` |
| `DB_URL` | URL JDBC utilizada pela aplicação para conexão com o banco de dados. | `jdbc:mysql://mysql:3306/roombooking` |
| `DB_USERNAME` | Usuário do banco de dados. | `root` |
| `DB_PASSWORD` | Senha do banco de dados. | `troqueEstaSenha123` |






# Agradecimentos / Referências: 

<br/> 

DevSuperior - Escola de programação 

----------

# Autora:

Sheila M. M. L. Silva 

https://www.linkedin.com/in/sheilasheila/



