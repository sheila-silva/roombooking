package com.roombooking.service;

import com.roombooking.domain.entity.Room;
import com.roombooking.dto.request.RoomRequest;
import com.roombooking.dto.response.RoomResponse;
import com.roombooking.exception.BusinessException;
import com.roombooking.exception.ResourceNotFoundException;
import com.roombooking.repository.RoomRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomService – regras de negócio")
class RoomServiceTest {

    @Mock private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private static final Long ROOM_ID = 1L;

    private RoomRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RoomRequest();
        validRequest.setName("Sala A");
        validRequest.setCapacity(10);
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve criar sala quando nome é único")
        void deveCriarSalaQuandoNomeUnico() {
            when(roomRepository.findByName("Sala A")).thenReturn(Optional.empty());
            when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

            RoomResponse response = roomService.create(validRequest);

            assertThat(response.getName()).isEqualTo("Sala A");
            assertThat(response.getCapacity()).isEqualTo(10);
            assertThat(response.isActive()).isTrue();
            verify(roomRepository).save(any(Room.class));
        }

        @Test
        @DisplayName("deve lançar BusinessException quando nome já está em uso")
        void deveLancarExcecaoQuandoNomeDuplicado() {
            Room existing = Room.create("Sala A", 5);
            when(roomRepository.findByName("Sala A")).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> roomService.create(validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Sala A")
                    .hasMessageContaining("already exists");

            verify(roomRepository, never()).save(any());
        }
    }

    // ── update() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("deve atualizar sala sem conflito de nome")
        void deveAtualizarSalaSemConflito() {
            Room existing = Room.create("Sala A", 10);

            when(roomRepository.existsByNameAndIdNot("Sala Atualizada", ROOM_ID)).thenReturn(false);
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(existing));
            when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

            RoomRequest updateRequest = new RoomRequest();
            updateRequest.setName("Sala Atualizada");
            updateRequest.setCapacity(20);
            updateRequest.setActive(false);

            RoomResponse response = roomService.update(ROOM_ID, updateRequest);

            assertThat(response.getName()).isEqualTo("Sala Atualizada");
            assertThat(response.getCapacity()).isEqualTo(20);
            assertThat(response.isActive()).isFalse();
        }

        @Test
        @DisplayName("deve lançar BusinessException quando novo nome já pertence a outra sala")
        void deveLancarExcecaoQuandoNomeDuplicadoNoUpdate() {
            when(roomRepository.existsByNameAndIdNot("Sala A", ROOM_ID)).thenReturn(true);

            assertThatThrownBy(() -> roomService.update(ROOM_ID, validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already exists");

            verify(roomRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando sala não existe")
        void deveLancarExcecaoQuandoSalaNaoExiste() {
            when(roomRepository.existsByNameAndIdNot(any(), eq(ROOM_ID))).thenReturn(false);
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roomService.update(ROOM_ID, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Room");
        }

        @Test
        @DisplayName("deve manter status atual quando active não é informado no request")
        void deveManterStatusAtualQuandoActiveNulo() {
            Room existing = Room.create("Sala A", 10); // active = true

            when(roomRepository.existsByNameAndIdNot("Sala A", ROOM_ID)).thenReturn(false);
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(existing));
            when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

            validRequest.setActive(null); // não informado

            RoomResponse response = roomService.update(ROOM_ID, validRequest);

            assertThat(response.isActive()).isTrue(); // manteve o original
        }
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete() – soft delete")
    class Delete {

        @Test
        @DisplayName("deve desativar sala (soft delete)")
        void deveDesativarSala() {
            Room room = Room.create("Sala A", 10);

            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
            when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

            roomService.delete(ROOM_ID);

            assertThat(room.isActive()).isFalse();
            verify(roomRepository).save(room);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando sala não existe")
        void deveLancarExcecaoQuandoSalaNaoExiste() {
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roomService.delete(ROOM_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Room");
        }
    }

    // ── findById() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar sala quando encontrada")
        void deveRetornarSalaQuandoEncontrada() {
            Room room = Room.create("Sala A", 10);
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

            RoomResponse response = roomService.findById(ROOM_ID);

            assertThat(response.getName()).isEqualTo("Sala A");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando sala não existe")
        void deveLancarExcecaoQuandoSalaNaoExiste() {
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roomService.findById(ROOM_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Room");
        }
    }
}