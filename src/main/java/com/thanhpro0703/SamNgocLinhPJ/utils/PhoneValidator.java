package com.thanhpro0703.SamNgocLinhPJ.utils;
import java.util.regex.Pattern;

public class PhoneValidator {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
}
