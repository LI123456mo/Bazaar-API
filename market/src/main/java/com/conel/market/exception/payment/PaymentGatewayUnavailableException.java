package com.conel.market.exception.payment;

/**
 * Thrown when payment gateway API is unreachable.
 * Triggers exponential backoff retry.
 */
public class PaymentGatewayUnavailableException extends PaymentException {
    public PaymentGatewayUnavailableException(String gatewayName) {
        super(String.format("Payment gateway %s is unavailable", gatewayName), 
              "GATEWAY_UNAVAILABLE");
    }

    public PaymentGatewayUnavailableException(String gatewayName, Throwable cause) {
        super(String.format("Payment gateway %s is unavailable", gatewayName), 
              "GATEWAY_UNAVAILABLE", cause);
    }
}
