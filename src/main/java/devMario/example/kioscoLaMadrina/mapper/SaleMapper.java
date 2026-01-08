package devMario.example.kioscoLaMadrina.mapper;

import devMario.example.kioscoLaMadrina.dto.SaleDetailDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import devMario.example.kioscoLaMadrina.model.Sale;
import devMario.example.kioscoLaMadrina.model.SaleDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(target = "username", source = "user.username")
    SaleResponseDTO toDTO(Sale sale);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    SaleDetailDTO toDetailDTO(SaleDetail detail);
}
