package com.example.splitwise;

/**
 * Utility class for checking if records exist in backend database
 * Used for authentication and validation purposes
 */
public class Exists {

    /**
     * Checks if data exists at the given API endpoint
     * @param url API endpoint URL
     * @return true if records exist, false otherwise
     */
    public static Boolean exist(String url) {

        String[][] rows = ApiCaller.ApiCaller1(url);

        int numlength = rows.length;
        if (numlength > 0) {
            return true;
        } else {
            return false;
        }
    }

}
