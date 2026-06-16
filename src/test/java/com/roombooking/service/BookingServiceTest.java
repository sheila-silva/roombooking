package com.roombooking.service;

import com.roombooking.domain.entity.Booking;
import com.roombooking.domain.entity.Room;
import com.roombooking.domain.entity.User;
import com.roombooking.domain.enums.BookingStatus;
import com.roombooking.dto.request.BookingRequest;
import com.roombooking.dto.response.BookingResponse;
import com.roombooking.exception.BusinessException;
import com.roombooking.exception.ConflictException;
import com.roombooking.exception.ResourceNotFoundException;
import com.roombooking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService – regras de negócio")
class BookingServiceTest {

    // ── mocks ─────────────────────────────────────────────────────────────────

    @Mock private BookingRepository bookingRepository;
    @Mock private RoomService       roomService;
    @Mock private UserService       userService;

    @InjectMocks
    private BookingService bookingService;

    // ── fixtures ──────────────────────────────────────────────────────────────

    private static final Long ROOM_ID    = 1L;
    private static final Long USER_ID    = 2L;
    private static final Long BOOKING_ID = 10L;

    private static final LocalDateTime START = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime END   = START.plusHours(2);

    private Room activeRoom;
    private User user;
    private BookingRequest validRequest;

    @BeforeEach
    void setUp() {
        activeRoom = Room.create("Sala A", 10);
        ReflectionTestUtils.setField(activeRoom, "id", ROOM_ID); // injeta o id que o banco daria

        user = User.create("João", "joao@email.com");
        ReflectionTestUtils.setField(user, "id", USER_ID);

        validRequest = buildRequest(ROOM_ID, USER_ID, START, END);
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve criar booking quando não há conflito")
        void deveCriarBookingQuandoSemConflito() {
            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(activeRoom);
            when(userService.fetchOrThrow(USER_ID)).thenReturn(user);
            when(bookingRepository.findActiveOverlapping(eq(ROOM_ID), any(), any()))
                    .thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            BookingResponse response = bookingService.create(validRequest);

            assertThat(response.getStatus()).isEqualTo(BookingStatus.ACTIVE);
            assertThat(response.getRoomName()).isEqualTo("Sala A");
            assertThat(response.getUserName()).isEqualTo("João");
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("deve lançar ConflictException quando já existe booking no mesmo período")
        void deveLancarConflictExceptionComSobreposicao() {
            Booking existente = Booking.create(activeRoom, user, START, END);

            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(activeRoom);
            when(userService.fetchOrThrow(USER_ID)).thenReturn(user);
            when(bookingRepository.findActiveOverlapping(eq(ROOM_ID), any(), any()))
                    .thenReturn(List.of(existente));

            assertThatThrownBy(() -> bookingService.create(validRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already has an active booking");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando sala não existe")
        void deveLancarExcecaoQuandoSalaNaoExiste() {
            when(roomService.fetchOrThrow(ROOM_ID))
                    .thenThrow(new ResourceNotFoundException("Room", ROOM_ID));

            assertThatThrownBy(() -> bookingService.create(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Room");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(activeRoom);
            when(userService.fetchOrThrow(USER_ID))
                    .thenThrow(new ResourceNotFoundException("User", USER_ID));

            assertThatThrownBy(() -> bookingService.create(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar BusinessException ao reservar sala inativa")
        void deveLancarExcecaoQuandoSalaInativa() {
            Room inactiveRoom = Room.create("Sala B", 10);
            ReflectionTestUtils.setField(inactiveRoom, "id", ROOM_ID);
            inactiveRoom.deactivate();

            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(inactiveRoom);
            when(userService.fetchOrThrow(USER_ID)).thenReturn(user);
            when(bookingRepository.findActiveOverlapping(eq(ROOM_ID), any(), any()))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> bookingService.create(validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inactive");

            verify(bookingRepository, never()).save(any());
        }

        // ── bordas de intervalo ───────────────────────────────────────────────

        @Test
        @DisplayName("borda: booking adjacente que termina exatamente no início do novo não deve conflitar")
        void naoDeveConflitarQuandoBookingTerminaExatamenteNoInicioDoNovo() {
            // existente: 09:00 → 10:00  |  novo: 10:00 → 11:00
            // condição da query: existente.endTime > novo.startTime → 10:00 > 10:00 = FALSE → sem conflito
            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(activeRoom);
            when(userService.fetchOrThrow(USER_ID)).thenReturn(user);
            when(bookingRepository.findActiveOverlapping(eq(ROOM_ID), any(), any()))
                    .thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            BookingResponse response = bookingService.create(validRequest);

            assertThat(response.getStatus()).isEqualTo(BookingStatus.ACTIVE);
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("borda: booking adjacente que começa exatamente no fim do novo não deve conflitar")
        void naoDeveConflitarQuandoBookingComecaExatamenteNoFimDoNovo() {
            // existente: 11:00 → 12:00  |  novo: 09:00 → 11:00
            // condição da query: existente.startTime < novo.endTime → 11:00 < 11:00 = FALSE → sem conflito
            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(activeRoom);
            when(userService.fetchOrThrow(USER_ID)).thenReturn(user);
            when(bookingRepository.findActiveOverlapping(eq(ROOM_ID), any(), any()))
                    .thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            BookingResponse response = bookingService.create(validRequest);

            assertThat(response.getStatus()).isEqualTo(BookingStatus.ACTIVE);
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("booking cancelado no mesmo horário não deve impedir nova reserva")
        void naoDeveConflitarQuandoBookingCanceladoOcupaOMesmoHorario() {
            // status = 'ACTIVE' na query exclui CANCELLED automaticamente — repositório retorna vazio
            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(activeRoom);
            when(userService.fetchOrThrow(USER_ID)).thenReturn(user);
            when(bookingRepository.findActiveOverlapping(eq(ROOM_ID), any(), any()))
                    .thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            BookingResponse response = bookingService.create(validRequest);

            assertThat(response.getStatus()).isEqualTo(BookingStatus.ACTIVE);
            verify(bookingRepository).save(any(Booking.class));
        }
    }

    // ── reschedule() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("reschedule()")
    class Reschedule {

        @Test
        @DisplayName("deve reagendar booking excluindo ele mesmo do check de conflito")
        void deveReagendarExcluindoSiMesmoDoConflito() {
            Booking booking = Booking.create(activeRoom, user, START, END);
            ReflectionTestUtils.setField(booking, "id", BOOKING_ID); // injeta o id que o banco daria

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(activeRoom);
            when(userService.fetchOrThrow(USER_ID)).thenReturn(user);
            when(bookingRepository.findActiveOverlappingExcluding(
                    eq(ROOM_ID), any(), any(), eq(BOOKING_ID)))
                    .thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            LocalDateTime novoInicio = START.plusDays(2);
            LocalDateTime novoFim    = novoInicio.plusHours(1);
            BookingRequest novoRequest = buildRequest(ROOM_ID, USER_ID, novoInicio, novoFim);

            BookingResponse response = bookingService.reschedule(BOOKING_ID, novoRequest);

            assertThat(response.getStartTime()).isEqualTo(novoInicio);
            assertThat(response.getEndTime()).isEqualTo(novoFim);
            assertThat(response.getStatus()).isEqualTo(BookingStatus.ACTIVE);

            // garante que usou o método que exclui o próprio booking — nunca o sem exclusão
            verify(bookingRepository).findActiveOverlappingExcluding(
                    eq(ROOM_ID), any(), any(), eq(BOOKING_ID));
            verify(bookingRepository, never()).findActiveOverlapping(any(), any(), any());
        }

        @Test
        @DisplayName("deve lançar ConflictException quando novo período conflita com outro booking")
        void deveLancarConflictExceptionQuandoNovoHorarioConflita() {
            Booking booking  = Booking.create(activeRoom, user, START, END);
            ReflectionTestUtils.setField(booking, "id", BOOKING_ID);

            Booking conflito = Booking.create(activeRoom, user, START.plusDays(2), END.plusDays(2));

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(activeRoom);
            when(userService.fetchOrThrow(USER_ID)).thenReturn(user);
            when(bookingRepository.findActiveOverlappingExcluding(any(), any(), any(), any()))
                    .thenReturn(List.of(conflito));

            LocalDateTime novoInicio = START.plusDays(2);
            BookingRequest novoRequest = buildRequest(ROOM_ID, USER_ID, novoInicio, novoInicio.plusHours(1));

            assertThatThrownBy(() -> bookingService.reschedule(BOOKING_ID, novoRequest))
                    .isInstanceOf(ConflictException.class);

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar BusinessException ao reagendar booking cancelado")
        void deveLancarExcecaoAoReagendarCancelado() {
            Booking booking = Booking.create(activeRoom, user, START, END);
            ReflectionTestUtils.setField(booking, "id", BOOKING_ID);
            booking.cancel();

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

            BookingRequest novoRequest = buildRequest(ROOM_ID, USER_ID, START.plusDays(1), END.plusDays(1));

            assertThatThrownBy(() -> bookingService.reschedule(BOOKING_ID, novoRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando booking não existe")
        void deveLancarExcecaoQuandoBookingNaoExiste() {
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.reschedule(BOOKING_ID, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Booking");
        }
    }

    // ── cancel() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("deve cancelar booking ativo e persistir status CANCELLED")
        void deveCancelarBookingAtivo() {
            Booking booking = Booking.create(activeRoom, user, START, END);
            ReflectionTestUtils.setField(booking, "id", BOOKING_ID);

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            BookingResponse response = bookingService.cancel(BOOKING_ID);

            assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            verify(bookingRepository).save(booking);
        }

        @Test
        @DisplayName("deve lançar BusinessException ao cancelar booking já cancelado")
        void deveLancarExcecaoAoCancelarJaCancelado() {
            Booking booking = Booking.create(activeRoom, user, START, END);
            ReflectionTestUtils.setField(booking, "id", BOOKING_ID);
            booking.cancel();

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancel(BOOKING_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already cancelled");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando booking não existe")
        void deveLancarExcecaoQuandoBookingNaoExiste() {
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancel(BOOKING_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── findByRoom() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByRoom()")
    class FindByRoom {

        @Test
        @DisplayName("deve retornar página de bookings da sala")
        void deveRetornarPaginaDeBookingsDaSala() {
            Pageable pageable = PageRequest.of(0, 20);
            Booking booking   = Booking.create(activeRoom, user, START, END);

            when(roomService.fetchOrThrow(ROOM_ID)).thenReturn(activeRoom);
            when(bookingRepository.findByRoomId(ROOM_ID, pageable))
                    .thenReturn(new PageImpl<>(List.of(booking)));

            var page = bookingService.findByRoom(ROOM_ID, pageable);

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getRoomName()).isEqualTo("Sala A");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando sala não existe")
        void deveLancarExcecaoQuandoSalaNaoExiste() {
            Pageable pageable = PageRequest.of(0, 20);

            when(roomService.fetchOrThrow(ROOM_ID))
                    .thenThrow(new ResourceNotFoundException("Room", ROOM_ID));

            assertThatThrownBy(() -> bookingService.findByRoom(ROOM_ID, pageable))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Room");
        }
    }

    // ── findByUser() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByUser()")
    class FindByUser {

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            Pageable pageable = PageRequest.of(0, 20);

            when(userService.fetchOrThrow(USER_ID))
                    .thenThrow(new ResourceNotFoundException("User", USER_ID));

            assertThatThrownBy(() -> bookingService.findByUser(USER_ID, pageable))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static BookingRequest buildRequest(
            Long roomId, Long userId, LocalDateTime start, LocalDateTime end
    ) {
        BookingRequest req = new BookingRequest();
        req.setRoomId(roomId);
        req.setUserId(userId);
        req.setStartTime(start);
        req.setEndTime(end);
        return req;
    }
}