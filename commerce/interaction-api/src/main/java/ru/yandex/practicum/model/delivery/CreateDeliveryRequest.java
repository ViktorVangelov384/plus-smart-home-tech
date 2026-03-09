package ru.yandex.practicum.model.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.model.warehouse.AddressDto;
import ru.yandex.practicum.model.warehouse.BookedProductsDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryRequest {
    private BookedProductsDto bookedProducts;
    private AddressDto fromAddress;
    private AddressDto toAddress;
}