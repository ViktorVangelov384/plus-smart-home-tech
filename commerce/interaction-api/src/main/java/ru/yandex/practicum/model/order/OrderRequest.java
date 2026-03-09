package ru.yandex.practicum.model.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.model.warehouse.AddressDto;
import ru.yandex.practicum.model.warehouse.ShoppingCartDto;

@NoArgsConstructor
@Getter
@Setter
public class OrderRequest {
    @NotNull
    @Valid
    private ShoppingCartDto shoppingCart;

    @NotNull
    private String username;

    @NotNull
    @Valid
    private AddressDto deliveryAddress;
}
