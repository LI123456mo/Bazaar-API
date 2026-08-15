package com.conel.market.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Example payload:
 * {
 *   "Body": {
 *     "stkCallback": {
 *       "MerchantRequestID": "123456-1234567-1",
 *       "CheckoutRequestID": "ws_CO_01081001293100",
 *       "ResultCode": 0,
 *       "ResultDesc": "The service request has been processed successfully.",
 *       "CallbackMetadata": {
 *         "Item": [
 *           {"Name": "Amount", "Value": 5000.0},
 *           {"Name": "MpesaReceiptNumber", "Value": "MPL12345678"},
 *           {"Name": "PhoneNumber", "Value": 254712345678},
 *           {"Name": "TransactionDate", "Value": 20260805172530}
 *         ]
 *       }
 *     }
 *   }
 * }
 */
public record DarajaWebhookPayload(
    @JsonProperty("Body")
    Body body
) {
    public record Body(
        @JsonProperty("stkCallback")
        StkCallback stkCallback
    ) {}

    public record StkCallback(
        @JsonProperty("MerchantRequestID")
        String merchantRequestId,

        @JsonProperty("CheckoutRequestID")
        String checkoutRequestId,

        @JsonProperty("ResultCode")
        Integer resultCode,

        @JsonProperty("ResultDesc")
        String resultDesc,

        @JsonProperty("CallbackMetadata")
        CallbackMetadata callbackMetadata
    ) {}

    public record CallbackMetadata(
        @JsonProperty("Item")
        java.util.List<CallbackItem> items
    ) {}

    public record CallbackItem(
        @JsonProperty("Name")
        String name,

        @JsonProperty("Value")
        Object value
    ) {}
}
