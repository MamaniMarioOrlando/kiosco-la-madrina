package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.exception.ResourceNotFoundException;
import devMario.example.kioscoLaMadrina.model.User;
import devMario.example.kioscoLaMadrina.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Operation(summary = "Update user avatar", description = "Updates a user's avatar image via base64 string.")
    @PatchMapping("/{id}/avatar")
    public ResponseEntity<?> updateAvatar(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con el ID especificado"));
                
        String avatarUrl = payload.get("avatarUrl");
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        
        return ResponseEntity.ok("Avatar updated successfully");
    }
}
