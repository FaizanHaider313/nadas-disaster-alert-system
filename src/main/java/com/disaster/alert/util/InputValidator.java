package com.disaster.alert.util;

/** Simple input validation helpers reused across controllers. */
public class InputValidator {

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidPassword(String pwd) {
        return pwd != null && pwd.length() >= 4;
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isNotEmpty(String... values) {
        for (String v : values) if (!isNotEmpty(v)) return false;
        return true;
    }
}