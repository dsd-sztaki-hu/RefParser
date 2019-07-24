package hu.sztaki.dsd.refparser;

import java.util.ArrayList;

/// Helper functions for the Parser.
final class HelperFunctions {

    private static Character[] romanNumerals = new Character[] { 'I', 'V', 'X', 'L', 'C', 'D', 'M' };

    public static boolean isRomanNumeral(String lexeme) {
        for (Character ch : lexeme.toCharArray()) {
            boolean any = false;
            for (Character rom : romanNumerals) {
                if (ch == rom) {
                    any = true;
                    break;
                }
            }
            if (!any)
                return false;
        }
        return true;
    }

    private static String[] monthNames = new String[] { "january", "february", "march", "april", "may", "june", "july",
            "august", "september", "october", "november", "december" };

    /// Gets the index of the month based on its name or its string index between 1
    /// and 12. If it's not valid, it returns null.
    public static Integer stringToMonth(String monthName) {
        for (int i = 0; i < 12; i++) {
            if (monthName.equalsIgnoreCase(monthNames[i]))
                return i + 1; // Months are indexed from 1.
        }

        Integer month = parseIntOrNull(withoutDot(monthName));
        return month != null && month >= 1 && month <= 12 ? month : null;
    }

    /// Determines if the given char is to be trimmed from the BEGNNING of a string.
    public static boolean isCharTrimmable(Character ch) {
        return Character.isWhitespace(ch) || ch == '&' || ch == '.';
    }

    /// Converts an ArrayList of strings to a CSLDateStruct.
    /// Only valid formats are accepted.
    public static CSLDateStruct datePartsToCSLDateStruct(ArrayList<String> parts) {
        if (parts.size() == 0) // Invalid input. Consider throwing an exception.
            return null;

        if (parts.contains("-")) { // If the input is a date range.
            int ind = parts.indexOf("-");
            ArrayList<String> startDate = new ArrayList<String>(parts.subList(0, ind));
            ArrayList<String> endDate = new ArrayList<String>(parts.subList(ind + 1, parts.size()));
            CSLDateStruct startStruct = datePartsToCSLDateStruct(startDate);
            CSLDateStruct endStruct = datePartsToCSLDateStruct(endDate);
            if (startStruct == null || endStruct == null)
                return null;
            startStruct.dateParts.add(endStruct.dateParts.get(0));
            return startStruct;
        }

        if (parts.size() == 1) { // It's just the year.
            Integer year = parseIntOrNull(parts.get(0));
            return year == null ? null : new CSLDateStruct(year);
        }

        if (parts.size() == 2) { // It's either MY or YM.
            Integer year = null, month = null;

            year = parseIntOrNull(parts.get(0));
            month = stringToMonth(parts.get(1));
            if (year == null || month == null) {
                year = parseIntOrNull(parts.get(1));
                month = stringToMonth(parts.get(0));
                if (year == null || month == null)
                    return null;
            }

            return new CSLDateStruct(year, month);
        }

        if (parts.size() == 3) { // It's either YMD or DMY.
            Integer year = null, month = null, day = null;

            year = parseIntOrNull(parts.get(0));
            month = stringToMonth(parts.get(1));
            day = stringToDay(parts.get(2));
            if (year == null || month == null || day == null) {
                year = parseIntOrNull(parts.get(2));
                month = stringToMonth(parts.get(1));
                day = stringToDay(parts.get(0));
                if (year == null || month == null || day == null)
                    return null;
            }

            return new CSLDateStruct(year, month, day);
        }

        return null;

    }

    /// Parses the given string into an Integer. Returns null on invalid input.
    public static Integer stringToDay(String dayString) {
        Integer day = Integer.parseInt(withoutDot(dayString));
        return day != null && day >= 1 && day <= 31 ? day : null;
    }

    /// This removes leading full stops, spaces, quotation marks and '&' signs, and
    /// trailing spaces.
    public static String trimAll(String str) {
        if (str == null || str.isEmpty())
            return str;

        StringBuilder sb = new StringBuilder();
        boolean beforeName = isCharTrimmable(str.charAt(0));
        for (Character ch : str.toCharArray()) {
            if (beforeName) {
                if (!isCharTrimmable(ch)) {
                    beforeName = false;
                    sb.append(ch);
                }
            } else
                sb.append(ch);
        }
        return sb.substring(0, sb.length());
    }

    /// Returns the string without the dot at the end.
    public static String withoutDot(String str) {
        return str != null && str.endsWith(".") ? str.substring(0, str.length() - 1) : str;
    }

    /// Determines if the given string contains any digits.
    public static boolean containsDigit(String str) {
        for (char ch : str.toCharArray()) {
            if (ch >= '0' && ch <= '9')
                return true;
        }
        return false;
    }

    /// Parses a string into an Integer. Returns null on invalid input.
    public static Integer parseIntOrNull(String num) {
        try {
            int value = Integer.parseInt(withoutDot(num));
            return value;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}