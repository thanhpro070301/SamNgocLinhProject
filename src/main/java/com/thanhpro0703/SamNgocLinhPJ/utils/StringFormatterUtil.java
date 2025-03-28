package com.thanhpro0703.SamNgocLinhPJ.utils;

public class StringFormatterUtil {
    public static String formatName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        name = name.trim().replaceAll("\\s+", " ");
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
    }
}