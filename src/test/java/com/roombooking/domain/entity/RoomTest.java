package com.roombooking.domain.entity;

import com.roombooking.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Room – regras de domínio")
class RoomTest {

    // ── Room.create() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve criar sala ativa com campos corretos")
        void deveCriarSalaAtiva() {
            Room room = Room.create("Sala A", 10);

            assertThat(room.getName()).isEqualTo("Sala A");
            assertThat(room.getCapacity()).isEqualTo(10);
            assertThat(room.isActive()).isTrue();
            assertThat(room.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("deve remover espaços extras do nome")
        void deveRemoverEspacosDoNome() {
            Room room = Room.create("  Sala B  ", 5);

            assertThat(room.getName()).isEqualTo("Sala B");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando capacidade é zero")
        void deveLancarExcecaoQuandoCapacidadeZero() {
            assertThatThrownBy(() -> Room.create("Sala C", 0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Capacity must be a positive value");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando capacidade é negativa")
        void deveLancarExcecaoQuandoCapacidadeNegativa() {
            assertThatThrownBy(() -> Room.create("Sala D", -5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Capacity must be a positive value");
        }
    }

    // ── Room.update() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("deve atualizar nome, capacidade e status")
        void deveAtualizarCampos() {
            Room room = Room.create("Sala A", 10);

            room.update("Sala Atualizada", 20, false);

            assertThat(room.getName()).isEqualTo("Sala Atualizada");
            assertThat(room.getCapacity()).isEqualTo(20);
            assertThat(room.isActive()).isFalse();
        }

        @Test
        @DisplayName("deve lançar BusinessException ao atualizar com capacidade inválida")
        void deveLancarExcecaoComCapacidadeInvalida() {
            Room room = Room.create("Sala A", 10);

            assertThatThrownBy(() -> room.update("Sala A", 0, true))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Capacity must be a positive value");
        }
    }

    // ── Room.deactivate() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("deactivate()")
    class Deactivate {

        @Test
        @DisplayName("deve marcar sala como inativa")
        void deveMarcarsalaComoInativa() {
            Room room = Room.create("Sala A", 10);

            room.deactivate();

            assertThat(room.isActive()).isFalse();
        }
    }

    // ── Room.assertActive() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("assertActive()")
    class AssertActive {

        @Test
        @DisplayName("não deve lançar exceção quando sala está ativa")
        void naoDeveLancarExcecaoQuandoAtiva() {
            Room room = Room.create("Sala A", 10);

            assertThatNoException().isThrownBy(room::assertActive);
        }

        @Test
        @DisplayName("deve lançar BusinessException quando sala está inativa")
        void deveLancarExcecaoQuandoInativa() {
            Room room = Room.create("Sala A", 10);
            room.deactivate();

            assertThatThrownBy(room::assertActive)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inactive");
        }
    }
}