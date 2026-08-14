package com.conel.market.entity.payment;


public enum PaymentMethod {
    M_PESA("M-Pesa", "Safaricom M-Pesa STK Push"),
    CARD("Card", "Debit/Credit Card via payment gateway"),
    COD("Cash on Delivery", "Payment on delivery"),
    WALLET("Wallet", "In-app wallet balance");

    private final String displayName;
    private final String description;

    PaymentMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
