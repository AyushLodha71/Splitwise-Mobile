package com.example.splitwise;
import java.util.Map;

public interface SplitFragment {
    // The fragments MUST have this exact parameter: double totalAmount
    Map<String, Double> getSplitData(double totalAmount);
    boolean isValid(double totalAmount);
}