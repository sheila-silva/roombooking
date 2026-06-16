package com.roombooking.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User – regras de domínio")
class UserTest {

    // ── User.create() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve criar usuário com nome e email normalizados")
        void deveCriarUsuarioNormalizado() {
            User user = User.create("  João Silva  ", "  JOAO@EMAIL.COM  ");

            assertThat(user.getName()).isEqualTo("João Silva");
            assertThat(user.getEmail()).isEqualTo("joao@email.com");
            assertThat(user.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("deve converter email para minúsculas")
        void deveConverterEmailParaMinusculas() {
            User user = User.create("Maria", "MARIA@EMAIL.COM");

            assertThat(user.getEmail()).isEqualTo("maria@email.com");
        }

        @Test
        @DisplayName("deve remover espaços extras do nome")
        void deveRemoverEspacosDoNome() {
            User user = User.create("  Carlos  ", "carlos@email.com");

            assertThat(user.getName()).isEqualTo("Carlos");
        }
    }

    // ── User.update() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("deve atualizar nome e email com normalização")
        void deveAtualizarCamposNormalizados() {
            User user = User.create("João", "joao@email.com");

            user.update("  João Atualizado  ", "  NOVO@EMAIL.COM  ");

            assertThat(user.getName()).isEqualTo("João Atualizado");
            assertThat(user.getEmail()).isEqualTo("novo@email.com");
        }
    }
}