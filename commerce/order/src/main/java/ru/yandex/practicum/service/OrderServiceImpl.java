package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exception.OrderNotFoundException;
import ru.yandex.practicum.feign.DeliveryClient;
import ru.yandex.practicum.feign.PaymentClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.delivery.CreateDeliveryRequest;
import ru.yandex.practicum.model.delivery.DeliveryCostRequest;
import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.model.order.OrderRequest;
import ru.yandex.practicum.model.order.ProductResponse;
import ru.yandex.practicum.model.warehouse.AddressDto;
import ru.yandex.practicum.model.warehouse.AssembleOrderRequest;
import ru.yandex.practicum.model.warehouse.BookedProductsDto;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseClient warehouseClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;

    private static final String ORDER_NOT_FOUND_MSG = "Заказ с ID %s не найден";

    @Override
    public List<OrderDto> getUsersOrders(String userName) {
        log.info("Получение списка заказов пользователя: {}", userName);
        List<Order> orders = orderRepository.findAllByUsername(userName);
        return orderMapper.toDtoList(orders);
    }

    @Transactional
    @Override
    public OrderDto createOrder(OrderRequest request) {
        log.info("Создание нового заказа для пользователя: {}", request.getUsername());

        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();

        BookedProductsDto bookedProducts = warehouseClient.checkProducts(request.getShoppingCart());

        CreateDeliveryRequest createRequest = new CreateDeliveryRequest(
                bookedProducts,
                warehouseAddress,
                request.getDeliveryAddress()
        );
        UUID deliveryId = deliveryClient.createDelivery(createRequest);

        DeliveryCostRequest costRequest = new DeliveryCostRequest(
                bookedProducts,
                request.getDeliveryAddress()
        );
        BigDecimal deliveryCost = deliveryClient.calculateDeliveryCost(costRequest);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setUsername(request.getUsername());
        order.setShoppingCartId(request.getShoppingCart().getCartId());
        order.setProducts(request.getShoppingCart().getProducts());
        order.setDeliveryVolume(BigDecimal.valueOf(bookedProducts.getTotalVolume()));
        order.setDeliveryWeight(BigDecimal.valueOf(bookedProducts.getTotalWeight()));
        order.setFragile(bookedProducts.getContainsFragile());
        order.setDeliveryPrice(deliveryCost);
        order.setDeliveryId(deliveryId);
        order.setState(OrderState.NEW);

        order.setDeliveryCountry(request.getDeliveryAddress().getCountry());
        order.setDeliveryCity(request.getDeliveryAddress().getCity());
        order.setDeliveryStreet(request.getDeliveryAddress().getStreet());
        order.setDeliveryHouse(request.getDeliveryAddress().getHouse());
        order.setDeliveryFlat(request.getDeliveryAddress().getFlat());

        Order savedOrder = orderRepository.save(order);
        log.info("Заказ {} создан, доставка {}", savedOrder.getOrderId(), deliveryId);

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    @Override
    public OrderDto payOrderSuccess(UUID orderId) {
        log.info("Успешная оплата заказа: {}", orderId);

        Order order = findOrderById(orderId);
        validateStateTransition(order.getState(), OrderState.PAID);

        OrderDto orderDto = orderMapper.toDto(order);
        paymentClient.createPayment(orderDto);

        order.setState(OrderState.PAID);
        Order updatedOrder = orderRepository.save(order);

        log.info("Статус заказа {} изменен на PAID", orderId);
        return orderMapper.toDto(updatedOrder);
    }

    @Transactional
    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Расчет полной стоимости заказа: {}", orderId);

        Order order = findOrderById(orderId);

        OrderDto orderDto = orderMapper.toDto(order);
        BigDecimal productCost = paymentClient.calculateProductCost(orderDto);
        order.setProductPrice(productCost);

        BigDecimal totalCost = paymentClient.calculateTotalCost(orderDto);
        order.setTotalPrice(totalCost);

        Order updatedOrder = orderRepository.save(order);
        log.info("Полная стоимость заказа {}: {}", orderId, totalCost);

        return orderMapper.toDto(updatedOrder);
    }

    @Transactional
    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Расчет стоимости доставки для заказа: {}", orderId);

        Order order = findOrderById(orderId);

        BookedProductsDto bookedProducts = new BookedProductsDto();
        bookedProducts.setTotalVolume(order.getDeliveryVolume().doubleValue());
        bookedProducts.setTotalWeight(order.getDeliveryWeight().doubleValue());
        bookedProducts.setContainsFragile(order.getFragile());

        AddressDto deliveryAddress = new AddressDto();
        deliveryAddress.setCountry(order.getDeliveryCountry());
        deliveryAddress.setCity(order.getDeliveryCity());
        deliveryAddress.setStreet(order.getDeliveryStreet());
        deliveryAddress.setHouse(order.getDeliveryHouse());
        deliveryAddress.setFlat(order.getDeliveryFlat());

        DeliveryCostRequest costRequest = new DeliveryCostRequest(bookedProducts, deliveryAddress);
        BigDecimal deliveryCost = deliveryClient.calculateDeliveryCost(costRequest);

        order.setDeliveryPrice(deliveryCost);
        Order updatedOrder = orderRepository.save(order);
        log.info("Стоимость доставки заказа {}: {}", orderId, deliveryCost);

        return orderMapper.toDto(updatedOrder);
    }

    @Transactional
    @Override
    public OrderDto changeOrderState(UUID orderId, OrderState state) {
        log.info("Изменение статуса заказа {} на {}", orderId, state);

        Order order = findOrderById(orderId);
        validateStateTransition(order.getState(), state);

        switch (state) {
            case ASSEMBLED:
                AssembleOrderRequest assembleRequest = new AssembleOrderRequest(order.getOrderId(), order.getProducts());
                warehouseClient.assembleOrder(assembleRequest);
                break;
            case ON_DELIVERY:
                warehouseClient.markShipped(order.getOrderId(), order.getDeliveryId());
                break;
            case PAYMENT_FAILED:
                break;
            case DELIVERY_FAILED:
                break;
            case ASSEMBLY_FAILED:
                break;
            default:
        }

        order.setState(state);
        Order updatedOrder = orderRepository.save(order);

        log.info("Статус заказа {} изменен на {}", orderId, state);
        return orderMapper.toDto(updatedOrder);
    }

    @Transactional
    @Override
    public OrderDto returnOrder(ProductResponse response) {
        log.info("Возврат заказа: {}", response.getOrderId());

        Order order = findOrderById(response.getOrderId());
        validateReturnPossibility(order);

        warehouseClient.returnProducts(response);

        order.setState(OrderState.PRODUCT_RETURNED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Заказ {} возвращен", order.getOrderId());
        return orderMapper.toDto(updatedOrder);
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Заказ не найден: {}", orderId);
                    return new OrderNotFoundException(
                            String.format(ORDER_NOT_FOUND_MSG, orderId));
                });
    }

    private void validateStateTransition(OrderState current, OrderState next) {
        boolean isValid = switch (current) {
            case NEW -> next == OrderState.ON_PAYMENT || next == OrderState.CANCELED;
            case ON_PAYMENT -> next == OrderState.PAID || next == OrderState.PAYMENT_FAILED || next == OrderState.CANCELED;
            case PAID -> next == OrderState.ASSEMBLED || next == OrderState.ON_DELIVERY;
            case ASSEMBLED -> next == OrderState.ON_DELIVERY || next == OrderState.ASSEMBLY_FAILED;
            case ON_DELIVERY -> next == OrderState.DELIVERED || next == OrderState.DELIVERY_FAILED;
            case DELIVERED -> next == OrderState.COMPLETED || next == OrderState.PRODUCT_RETURNED;
            case COMPLETED -> next == OrderState.PRODUCT_RETURNED;
            case PAYMENT_FAILED -> next == OrderState.ON_PAYMENT || next == OrderState.CANCELED;
            case DELIVERY_FAILED -> next == OrderState.ON_DELIVERY || next == OrderState.CANCELED;
            case ASSEMBLY_FAILED -> next == OrderState.ASSEMBLED || next == OrderState.CANCELED;
            case PRODUCT_RETURNED -> next == OrderState.COMPLETED;
            default -> false;
        };

        if (!isValid) {
            throw new IllegalStateException(
                    String.format("Недопустимый переход статуса: %s -> %s", current, next)
            );
        }
    }

    private void validateReturnPossibility(Order order) {
        if (order.getState() != OrderState.DELIVERED &&
                order.getState() != OrderState.COMPLETED) {
            throw new IllegalStateException(
                    String.format("Нельзя вернуть заказ в статусе: %s", order.getState())
            );
        }
    }
}