package com.roombooking.service;

import com.roombooking.domain.entity.User;
import com.roombooking.dto.request.UserRequest;
import com.roombooking.dto.response.UserResponse;
import com.roombooking.exception.BusinessException;
import com.roombooking.exception.ResourceNotFoundException;
import com.roombooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService – regras de negócio")
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static final Long USER_ID = 1L;

    private UserRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new UserRequest();
        validRequest.setName("João Silva");
        validRequest.setEmail("joao@email.com");
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve criar usuário quando email é único")
        void deveCriarUsuarioQuandoEmailUnico() {
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponse response = userService.create(validRequest);

            assertThat(response.getName()).isEqualTo("João Silva");
            assertThat(response.getEmail()).isEqualTo("joao@email.com");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("deve normalizar email antes de verificar unicidade")
        void deveNormalizarEmailAntesDeVerificarUnicidade() {
            validRequest.setEmail("  JOAO@EMAIL.COM  ");

            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.create(validRequest);

            verify(userRepository).findByEmail("joao@email.com");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando email já está em uso")
        void deveLancarExcecaoQuandoEmailDuplicado() {
            User existing = User.create("Outro", "joao@email.com");
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> userService.create(validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already in use");

            verify(userRepository, never()).save(any());
        }
    }

    // ── update() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("deve atualizar usuário quando email não pertence a outro usuário")
        void deveAtualizarUsuarioQuandoEmailDisponivel() {
            User existing = User.create("João", "joao@email.com");

            when(userRepository.existsByEmailAndIdNot("joao@email.com", USER_ID)).thenReturn(false);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserRequest updateRequest = new UserRequest();
            updateRequest.setName("João Atualizado");
            updateRequest.setEmail("joao@email.com");

            UserResponse response = userService.update(USER_ID, updateRequest);

            assertThat(response.getName()).isEqualTo("João Atualizado");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando email pertence a outro usuário")
        void deveLancarExcecaoQuandoEmailDuplicadoNoUpdate() {
            when(userRepository.existsByEmailAndIdNot("joao@email.com", USER_ID)).thenReturn(true);

            assertThatThrownBy(() -> userService.update(USER_ID, validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already in use");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            when(userRepository.existsByEmailAndIdNot(any(), eq(USER_ID))).thenReturn(false);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.update(USER_ID, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("deve deletar usuário existente")
        void deveDeletarUsuarioExistente() {
            User user = User.create("João", "joao@email.com");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            userService.delete(USER_ID);

            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.delete(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");

            verify(userRepository, never()).delete(any());
        }
    }

    // ── findById() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar usuário quando encontrado")
        void deveRetornarUsuarioQuandoEncontrado() {
            User user = User.create("João", "joao@email.com");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            UserResponse response = userService.findById(USER_ID);

            assertThat(response.getName()).isEqualTo("João");
            assertThat(response.getEmail()).isEqualTo("joao@email.com");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }
}