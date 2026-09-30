package devMario.example.kioscoLaMadrina.config;

import devMario.example.kioscoLaMadrina.dto.CreateUserRequestDTO;
import devMario.example.kioscoLaMadrina.model.Role;
import devMario.example.kioscoLaMadrina.service.UserService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private UserService userService;

    @Test
    void whenAdminAlreadyExists_DoesNothing() {
        when(userService.adminExists()).thenReturn(true);

        bootstrap("admin", "AdminPass123!").run(null);

        verify(userService, never()).create(any());
    }

    @Test
    void whenNoAdminAndNoCredentials_DoesNotCreateAnything() {
        when(userService.adminExists()).thenReturn(false);

        bootstrap("", "").run(null);

        verify(userService, never()).create(any());
    }

    @Test
    void whenNoAdmin_CreatesOneWithAdminRole() {
        when(userService.adminExists()).thenReturn(false);

        bootstrap("admin", "AdminPass123!").run(null);

        ArgumentCaptor<CreateUserRequestDTO> captor = ArgumentCaptor.forClass(CreateUserRequestDTO.class);
        verify(userService).create(captor.capture());
        assertThat(captor.getValue().username()).isEqualTo("admin");
        assertThat(captor.getValue().role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void whenPasswordIsWeak_FailsFastInsteadOfCreatingAnInsecureAdmin() {
        when(userService.adminExists()).thenReturn(false);

        assertThatThrownBy(() -> bootstrap("admin", "123").run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("password");

        verify(userService, never()).create(any());
    }

    private AdminBootstrap bootstrap(String username, String password) {
        AdminBootstrapProperties properties = new AdminBootstrapProperties(username, password, "admin@kiosco.test");
        return new AdminBootstrap(properties, userService, validator);
    }
}
