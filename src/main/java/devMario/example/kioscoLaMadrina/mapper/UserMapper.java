package devMario.example.kioscoLaMadrina.mapper;

import devMario.example.kioscoLaMadrina.dto.UserResponseDTO;
import devMario.example.kioscoLaMadrina.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toDTO(User user);
}
