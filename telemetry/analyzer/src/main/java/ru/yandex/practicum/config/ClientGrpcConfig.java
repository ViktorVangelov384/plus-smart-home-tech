package ru.yandex.practicum.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Slf4j
@Configuration
public class ClientGrpcConfig {

    @Value("${grpc.client.hub-router.address}")
    private String routerServiceAddress;

    @Value("${grpc.client.hub-router.enable-keep-alive:true}")
    private boolean enableKeepAlive;

    @Value("${grpc.client.hub-router.keep-alive-without-calls:true}")
    private boolean maintainKeepAlive;

    @Bean
    public ManagedChannel configureRouterChannel() {
        log.info("Инициализация gRPC канала к сервису маршрутизации: {}", routerServiceAddress);

        ConnectionDetails connectionDetails = parseConnectionAddress(routerServiceAddress);

        ManagedChannel communicationChannel = ManagedChannelBuilder
                .forAddress(connectionDetails.hostname(), connectionDetails.port())
                .usePlaintext()
                .enableRetry()
                .keepAliveWithoutCalls(maintainKeepAlive)
                .build();

        log.info("gRPC канал успешно установлен. Сервер: {}:{}",
                connectionDetails.hostname(), connectionDetails.port());

        return communicationChannel;
    }

    @Bean
    public HubRouterControllerGrpc.HubRouterControllerBlockingStub createRouterClient(ManagedChannel communicationChannel) {
        log.debug("Формирование gRPC клиента для сервиса маршрутизации");

        HubRouterControllerGrpc.HubRouterControllerBlockingStub routerClient =
                HubRouterControllerGrpc.newBlockingStub(communicationChannel);

        log.info("gRPC клиент сервиса маршрутизации готов к использованию");
        return routerClient;
    }

    private ConnectionDetails parseConnectionAddress(String address) {
        String cleanedAddress = address.replace("static://", "");
        String[] addressComponents = cleanedAddress.split(":");

        if (addressComponents.length != 2) {
            throw new IllegalArgumentException(
                    String.format("Некорректный формат адреса gRPC сервера: %s", address)
            );
        }

        String host = addressComponents[0];
        int port = Integer.parseInt(addressComponents[1]);

        return new ConnectionDetails(host, port);
    }

    private record ConnectionDetails(String hostname, int port) {
    }
}
