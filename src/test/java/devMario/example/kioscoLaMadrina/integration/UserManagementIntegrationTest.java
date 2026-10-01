package devMario.example.kioscoLaMadrina.integration;

import com.jayway.jsonpath.JsonPath;
import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la cadena de seguridad real (login → JWT → filtro → endpoint) contra PostgreSQL.
 * No usa @WithMockUser a propósito: los agujeros de la Fase 1 estaban justamente en esa cadena.
 */
@ActiveProfiles("test")
class UserManagementIntegrationTest extends AbstractIntegrationTest {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "AdminPass123!";
    private static final String EMPLOYEE_PASSWORD = "EmployeePass123";
    private static final String VALID_AVATAR = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ==";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // --- Registro y alta de usuarios ---

    @Test
    void publicSignupEndpoint_NoLongerExists() throws Exception {
        // 401 y no 404: con "denegar por defecto" un anónimo no puede averiguar qué rutas existen.
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"hacker","email":"h@x.com","password":"123456","role":["admin"]}
                                """))
                .andExpect(status().isUnauthorized());

        assertThat(userRepository.findByUsername("hacker")).isEmpty();
    }

    @Test
    void bootstrapAdmin_IsCreatedOnStartup_AndCanLogin() throws Exception {
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    void admin_CanCreateEmployee_WhoCanThenLogin() throws Exception {
        String username = uniqueUsername();

        createUser(adminToken(), username, "EMPLOYEE")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, EMPLOYEE_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("EMPLOYEE"));
    }

    @Test
    void employee_CannotCreateUsers() throws Exception {
        String employeeToken = createEmployeeAndLogin();

        createUser(employeeToken, uniqueUsername(), "ADMIN")
                .andExpect(status().isForbidden());
    }

    @Test
    void employee_GetsForbidden_EvenWithAnInvalidBody_SoValidationRulesAreNotLeaked() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearer(createEmployeeAndLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void employee_CannotListUsers() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", bearer(createEmployeeAndLogin())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_WithDuplicateUsername_Returns409() throws Exception {
        String username = uniqueUsername();
        createUser(adminToken(), username, "EMPLOYEE").andExpect(status().isCreated());

        createUser(adminToken(), username, "EMPLOYEE")
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_WithInvalidRole_Returns400() throws Exception {
        createUser(adminToken(), uniqueUsername(), "SUPERUSER")
                .andExpect(status().isBadRequest());
    }

    // --- Login ---

    @Test
    void login_WithWrongPassword_Returns401() throws Exception {
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(ADMIN_USERNAME, "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    // --- Usuarios inactivos ---

    @Test
    void deactivatedUser_LosesAccessImmediately_AndCannotLoginAgain() throws Exception {
        String username = uniqueUsername();
        Long id = createEmployee(username);
        String employeeToken = login(username, EMPLOYEE_PASSWORD);

        mockMvc.perform(patch("/api/users/{id}/status", id)
                        .header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // El token emitido antes de la desactivación deja de servir
        mockMvc.perform(get("/api/products").header("Authorization", bearer(employeeToken)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, EMPLOYEE_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_CannotDeactivateThemselves() throws Exception {
        Long adminId = userRepository.findByUsername(ADMIN_USERNAME).orElseThrow().getId();

        mockMvc.perform(patch("/api/users/{id}/status", adminId)
                        .header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findByUsername(ADMIN_USERNAME).orElseThrow().isActive()).isTrue();
    }

    // --- Avatar ---

    @Test
    void user_CanUpdateOwnAvatar() throws Exception {
        String username = uniqueUsername();
        createEmployee(username);

        updateMyAvatar(login(username, EMPLOYEE_PASSWORD), VALID_AVATAR)
                .andExpect(status().isNoContent());

        assertThat(userRepository.findByUsername(username).orElseThrow().getAvatarUrl()).isEqualTo(VALID_AVATAR);
    }

    @Test
    void user_CannotUpdateAnotherUsersAvatar_ByIdAnymore() throws Exception {
        Long adminId = userRepository.findByUsername(ADMIN_USERNAME).orElseThrow().getId();
        String avatarBefore = userRepository.findById(adminId).orElseThrow().getAvatarUrl();

        mockMvc.perform(patch("/api/users/{id}/avatar", adminId)
                        .header("Authorization", bearer(createEmployeeAndLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatarUrl\": \"" + VALID_AVATAR + "\"}"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isGreaterThanOrEqualTo(400));

        assertThat(userRepository.findById(adminId).orElseThrow().getAvatarUrl()).isEqualTo(avatarBefore);
    }

    @Test
    void avatar_ThatIsNotAnAllowedImageDataUrl_Returns400() throws Exception {
        String token = createEmployeeAndLogin();

        updateMyAvatar(token, "data:image/svg+xml;base64,PHN2Zz48L3N2Zz4=").andExpect(status().isBadRequest());
        updateMyAvatar(token, "https://evil.example/tracker.png").andExpect(status().isBadRequest());
    }

    @Test
    void avatar_LargerThanLimit_Returns400() throws Exception {
        String hugeAvatar = "data:image/jpeg;base64," + "A".repeat(300_000);

        updateMyAvatar(createEmployeeAndLogin(), hugeAvatar).andExpect(status().isBadRequest());
    }

    // --- Helpers ---

    private String adminToken() throws Exception {
        return login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    private String createEmployeeAndLogin() throws Exception {
        String username = uniqueUsername();
        createEmployee(username);
        return login(username, EMPLOYEE_PASSWORD);
    }

    private Long createEmployee(String username) throws Exception {
        MvcResult result = createUser(adminToken(), username, "EMPLOYEE")
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private ResultActions createUser(String token, String username, String role)
            throws Exception {
        return mockMvc.perform(post("/api/users")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"%s","email":"%s@kiosco.test","password":"%s","role":"%s"}
                        """.formatted(username, username, EMPLOYEE_PASSWORD, role)));
    }

    private ResultActions updateMyAvatar(String token, String avatarUrl)
            throws Exception {
        return mockMvc.perform(patch("/api/users/me/avatar")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"avatarUrl\": \"" + avatarUrl + "\"}"));
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private static String credentials(String username, String password) {
        return "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String uniqueUsername() {
        return "emp_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
