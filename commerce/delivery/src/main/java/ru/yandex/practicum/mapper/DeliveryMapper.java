package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.model.delivery.DeliveryDto;
import ru.yandex.practicum.model.warehouse.AddressDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliveryMapper {

    @Mapping(target = "deliveryId", source = "delivery.deliveryId")
    @Mapping(target = "orderId", source = "delivery.orderId")
    @Mapping(target = "deliveryState", source = "delivery.deliveryState")
    @Mapping(target = "fromAddress", expression = "java(mapFromAddress(delivery))")
    @Mapping(target = "toAddress", expression = "java(mapToAddress(delivery))")
    @Mapping(target = "totalVolume", source = "delivery.totalVolume")
    @Mapping(target = "totalWeight", source = "delivery.totalWeight")
    @Mapping(target = "fragile", source = "delivery.fragile")
    @Mapping(target = "deliveryCost", source = "delivery.deliveryCost")
    DeliveryDto toDto(Delivery delivery);

    default AddressDto mapFromAddress(Delivery delivery) {
        if (delivery == null) return null;

        AddressDto address = new AddressDto();
        address.setCountry(delivery.getCountryFrom());
        address.setCity(delivery.getCityFrom());
        address.setStreet(delivery.getStreetFrom());
        address.setHouse(delivery.getHouseFrom());
        address.setFlat(delivery.getFlatFrom());
        return address;
    }

    default AddressDto mapToAddress(Delivery delivery) {
        if (delivery == null) return null;

        AddressDto address = new AddressDto();
        address.setCountry(delivery.getCountryTo());
        address.setCity(delivery.getCityTo());
        address.setStreet(delivery.getStreetTo());
        address.setHouse(delivery.getHouseTo());
        address.setFlat(delivery.getFlatTo());
        return address;
    }
}