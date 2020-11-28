package com.hal9000.warehouse.inventory.adapter.in.error;

import lombok.Value;

@Value
public class ErrorResponse {
    String code;
    String message;
}
