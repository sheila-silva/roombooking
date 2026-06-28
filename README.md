## 🎬 Room Booking

<br/> 

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat)](LICENSE)

<br/>

Room Booking é uma API REST para revervas de salas, desenvolvida com Spring Boot. Ela permite cadastrar salas e usuários, realizar reservas com validação de conflito de horários, reagendar e cancelar reservas, tudo com paginação e tratamento de erros centralizados. 


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









# Agradecimentos / Referências: 

<br/> 

DevSuperior - Escola de programação 

----------

# Autora:

Sheila M. M. L. Silva 

https://www.linkedin.com/in/sheilasheila/



