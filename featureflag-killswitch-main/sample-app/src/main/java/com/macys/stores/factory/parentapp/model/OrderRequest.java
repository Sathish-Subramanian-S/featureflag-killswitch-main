package com.macys.stores.factory.parentapp.model;

import lombok.Data;

@Data
public class OrderRequest {
    private String customerId;
    private java.util.List<String> itemIds;
}
