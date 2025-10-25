//package com.macys.stores.factory.parentapp.service;
//
//import com.macys.stores.factory.parentapp.model.OrderRequest;
//import com.macys.stores.factory.parentapp.model.OrderResponse;
//import com.example.webclientlaunchdarklylib.service.WebClientService;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//@Service
//public class OrderService {
//
//    private final WebClientService webClientService;
//
//    public OrderService(WebClientService webClientService) {
//        this.webClientService = webClientService;
//    }
//
//    public Mono<OrderResponse> createOrder(OrderRequest orderRequest) {
//        String serviceName = "orderService";
//        return webClientService.post(
//                serviceName,
//                OrderResponse.class,
//                orderRequest,
//                null,
//                error -> Mono.empty()
//        );
//    }
//}
