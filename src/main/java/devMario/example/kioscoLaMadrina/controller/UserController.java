package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.CreateUserRequestDTO;
import devMario.example.kioscoLaMadrina.dto.UpdateAvatarRequestDTO;
import devMario.example.kioscoLaMadrina.dto.UpdateUserStatusRequestDTO;
import devMario.example.kioscoLaMadrina.dto.UserResponseDTO;
import devMario.example.kioscoLaMadrina.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "List users", description = "Requires ADMIN role.")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponseDTO> list() {
        return userService.findAll();
    }

    @Operation(summary = "Create user", description = "Creates an employee or admin account. Requires ADMIN role.")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@Valid @RequestBody CreateUserRequestDTO request) {
        return userService.create(request);
    }

    @Operation(summary = "Activate or deactivate user", description = "A deactivated user loses access immediately. Requires ADMIN role.")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserResponseDTO updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody UpdateUserStatusRequestDTO request,
                                        @AuthenticationPrincipal UserDetails currentUser) {
        return userService.updateStatus(id, request.active(), currentUser.getUsername());
    }

    // El usuario sale del token, nunca de la URL: así nadie puede modificar el perfil de otro.
    @Operation(summary = "Update own avatar", description = "Updates the authenticated user's avatar (PNG/JPEG/WEBP data URL).")
    @PatchMapping("/me/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyAvatar(@Valid @RequestBody UpdateAvatarRequestDTO request,
                               @AuthenticationPrincipal UserDetails currentUser) {
        userService.updateAvatar(currentUser.getUsername(), request.avatarUrl());
    }
}
