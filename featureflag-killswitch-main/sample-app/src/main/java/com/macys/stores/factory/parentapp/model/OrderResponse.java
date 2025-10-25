package com.macys.stores.factory.parentapp.model;

import lombok.Data;

@Data
public class OrderResponse {
    private String orderId;
    private String status;
    private double total;
}