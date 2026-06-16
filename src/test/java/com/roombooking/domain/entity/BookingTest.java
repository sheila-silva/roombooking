package com.roombooking.domain.entity;

import com.roombooking.domain.enums.BookingStatus;
import com.roombooking.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Booking – regras de domínio")
class BookingTest {

    // ── fixtures ─────────────────────────────────────────────────────────────

    private static final LocalDateTime NOW   = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime LATER = NOW.plusHours(2);

    private Room activeRoom;
    private Room inactiveRoom;
    private User user;

    @BeforeEach
    void setUp() {
        activeRoom   = Room.create("Sala A", 10);
        inactiveRoom = Room.create("Sala B", 10);
        inactiveRoom.deactivate();
        user = User.create("João", "joao@email.com");
    }

    // ── Booking.create() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve criar booking ACTIVE com campos corretos")
        void devecriarBookingAtivo() {
            Booking booking = Booking.create(activeRoom, user, NOW, LATER);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACTIVE);
            assertThat(booking.getRoom()).isSameAs(activeRoom);
            assertThat(booking.getUser()).isSameAs(user);
            assertThat(booking.getStartTime()).isEqualTo(NOW);
            assertThat(booking.getEndTime()).isEqualTo(LATER);
            assertThat(booking.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("deve lançar BusinessException quando endTime == startTime")
        void deveLancarExcecaoQuandoEndTimeIgualStartTime() {
            assertThatThrownBy(() -> Booking.create(activeRoom, user, NOW, NOW))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("End time must be strictly after start time");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando endTime é anterior a startTime")
        void deveLancarExcecaoQuandoEndTimeAnteriorStartTime() {
            LocalDateTime antes = NOW.minusMinutes(30);

            assertThatThrownBy(() -> Booking.create(activeRoom, user, NOW, antes))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("End time must be strictly after start time");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando startTime é null")
        void deveLancarExcecaoQuandoStartTimeNulo() {
            assertThatThrownBy(() -> Booking.create(activeRoom, user, null, LATER))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Start and end times must not be null");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando endTime é null")
        void deveLancarExcecaoQuandoEndTimeNulo() {
            assertThatThrownBy(() -> Booking.create(activeRoom, user, NOW, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Start and end times must not be null");
        }

        @Test
        @DisplayName("deve lançar BusinessException ao reservar sala inativa")
        void deveLancarExcecaoQuandoSalaInativa() {
            assertThatThrownBy(() -> Booking.create(inactiveRoom, user, NOW, LATER))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inactive");
        }
    }

    // ── Booking.cancel() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("deve mudar status para CANCELLED")
        void deveMudarStatusParaCancelado() {
            Booking booking = Booking.create(activeRoom, user, NOW, LATER);

            booking.cancel();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("deve lançar BusinessException ao cancelar booking já cancelado")
        void deveLancarExcecaoAoCancelarJaCancelado() {
            Booking booking = Booking.create(activeRoom, user, NOW, LATER);
            booking.cancel();

            assertThatThrownBy(booking::cancel)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already cancelled");
        }
    }

    // ── Booking.reschedule() ─────────────────────────────────────────────────

    @Nested
    @DisplayName("reschedule()")
    class Reschedule {

        @Test
        @DisplayName("deve atualizar startTime e endTime")
        void deveAtualizarHorarios() {
            Booking booking   = Booking.create(activeRoom, user, NOW, LATER);
            LocalDateTime novoInicio = NOW.plusDays(1);
            LocalDateTime novoFim    = novoInicio.plusHours(1);

            booking.reschedule(novoInicio, novoFim);

            assertThat(booking.getStartTime()).isEqualTo(novoInicio);
            assertThat(booking.getEndTime()).isEqualTo(novoFim);
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACTIVE);
        }

        @Test
        @DisplayName("deve lançar BusinessException ao reagendar booking cancelado")
        void deveLancarExcecaoAoReagendarCancelado() {
            Booking booking = Booking.create(activeRoom, user, NOW, LATER);
            booking.cancel();

            assertThatThrownBy(() -> booking.reschedule(NOW.plusDays(1), LATER.plusDays(1)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando novo endTime é anterior ao novo startTime")
        void deveLancarExcecaoComDatasInvalidas() {
            Booking booking = Booking.create(activeRoom, user, NOW, LATER);

            assertThatThrownBy(() -> booking.reschedule(LATER, NOW))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("End time must be strictly after start time");
        }
    }

    // ── Booking.assertActive() ────────────────────────────────────────────────

    @Nested
    @DisplayName("assertActive()")
    class AssertActive {

        @Test
        @DisplayName("não deve lançar exceção quando booking está ACTIVE")
        void naoDeveLancarExcecaoQuandoAtivo() {
            Booking booking = Booking.create(activeRoom, user, NOW, LATER);

            assertThatNoException().isThrownBy(booking::assertActive);
        }

        @Test
        @DisplayName("deve lançar BusinessException quando booking está CANCELLED")
        void deveLancarExcecaoQuandoCancelado() {
            Booking booking = Booking.create(activeRoom, user, NOW, LATER);
            booking.cancel();

            assertThatThrownBy(booking::assertActive)
                    .isInstanceOf(BusinessException.class);
        }
    }
}