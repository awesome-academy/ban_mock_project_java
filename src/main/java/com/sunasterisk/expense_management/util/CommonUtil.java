package com.sunasterisk.expense_management.util;

public class CommonUtil {
    public static Boolean isSafeRedirectUrl(String url) {
        // Basic check to prevent open redirect vulnerabilities
        return url != null && !url.isBlank() && url.startsWith("/") && !url.startsWith("//") && !url.contains("://");
    }
}
