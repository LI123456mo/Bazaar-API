package com.conel.market.service.payment;

import java.util.Set;

/**
 * Decides whether a failed M-Pesa STK push is worth retrying automatically.
 * Temporary problems are retried; decisions by the customer are not.
 */
public final class MpesaFailureClassifier {

    private MpesaFailureClassifier() {}

    // Temporary problems on Safaricom's side or the customer's phone
    private static final Set<Integer> RETRYABLE = Set.of(
            1037,  // prompt timed out, customer didn't respond / unreachable
            9999   // error sending the push request
    );

    // For reference, final (no auto-retry) codes include:
    // 1    insufficient balance
    // 1032 customer cancelled
    // 2001 wrong PIN

    public static boolean isRetryable(Integer resultCode) {
        if (resultCode == null) return false;
        return RETRYABLE.contains(resultCode);
    }
}