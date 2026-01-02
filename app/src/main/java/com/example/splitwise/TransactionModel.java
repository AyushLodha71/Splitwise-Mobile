package com.example.splitwise;

/**
 * Data model representing a transaction/expense
 */
public class TransactionModel {
    public String payee, amount, reason, type;

    public TransactionModel(String payee, String amount, String reason, String type) {
        this.payee = payee;
        this.amount = amount;
        this.reason = reason;
        this.type = type;
    }
}