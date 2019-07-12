package hu.sztaki.dsd.refparser;

import java.util.ArrayList;

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

    // Gets the index of the month based on its name or its string index between 1
    // and 12. If it's not valid, it returns null.
    public static Integer stringToMonth(String monthName) {
        for (int i = 0; i < 12; i++) {
            if (monthName.equalsIgnoreCase(monthNames[i]))
                return i + 1; // Months are indexed from 1.
        }

        try {
            int month = Integer.parseInt(monthName);
            if (month >= 1 && month <= 12)
                return month;
        } catch (NumberFormatException ex) {
            return null;
        }

        return null;
    }

    // Determines if the given char is to be trimmed from the BEGNNING of a string.
    public static boolean isCharTrimmable(Character ch) {
        return Character.isWhitespace(ch) || ch == '&' || ch == '.';
    }

    // Converts an ArrayList of strings to a CSLDateStruct.
    public static CSLDateStruct datePartsToCSLDateStruct(ArrayList<String> parts) {
        if (parts.size() == 0) // Invalid input. Consider throwing an exception.
            return null;

        if (parts.size() == 1) { // It's just the year.
            int year;
            try {
                year = Integer.parseInt(parts.get(0));
            } catch (NumberFormatException ex) {
                return null;
            }
            return new CSLDateStruct(year);
        }

        if (parts.size() == 2) { // It's either [month, year] or [year, month].
            Integer year = null, month = null;
            try {
                year = Integer.parseInt(parts.get(0));
                month = stringToMonth(parts.get(1));
                if (year == null || month == null)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex0) {
                try {
                    year = Integer.parseInt(parts.get(1));
                    month = stringToMonth(parts.get(0));
                    if (year == null || month == null)
                        throw new NumberFormatException();
                } catch (NumberFormatException ex1) {
                    return null;
                }
            }
            return new CSLDateStruct(year, month);
        }

        if (parts.size() == 3) { // It's either YMD or DMY.
            Integer year = null, month = null, day = null;
            try {
                year = Integer.parseInt(parts.get(0));
                month = stringToMonth(parts.get(1));
                day = stringToDay(parts.get(2));
                if (year == null || month == null || day == null)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex0) {
                try {
                    year = Integer.parseInt(parts.get(2));
                    month = stringToMonth(parts.get(1));
                    day = stringToDay(parts.get(0));
                    if (year == null || month == null || day == null)
                        throw new NumberFormatException();
                } catch (NumberFormatException ex1) {
                    return null;
                }
            }
            return new CSLDateStruct(year, month, day);
        }

        return null; // TODO: more formats
    }

    // Parses the given string into an Integer. Returns null on invalid input.
    public static Integer stringToDay(String dayString) {
        try {
            int day = Integer.parseInt(dayString);
            return day >= 1 && day <= 31 ? day : null;

        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // Returns null on invalid input.
    public static String parseNumOfPages(String volumeString) {
        StringBuilder numOfPages = null;
        for (Character ch : volumeString.toCharArray()) {
            if (ch == '(')
                numOfPages = new StringBuilder();
            else if (ch != ')' && numOfPages != null)
                numOfPages.append(ch);
        }
        return numOfPages == null ? null : numOfPages.toString();
    }

    // This removes leading full stops, spaces, quotation marks and '&' signs, and
    // trailing spaces.
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
        // int trimFromEnd = 0;
        /*
         * for (int i = sb.length() - 1; i >= 0; i--) { if
         * (isCharTrimmable(sb.charAt(i)) && sb.charAt(i) != '.') // Dots can't be
         * trimmed from the end of names. trimFromEnd++; }
         */
        return sb.substring(0, sb.length()/* - trimFromEnd */);
    }

    // Returns the string without the dot at the end.
    public static String withoutDot(String str) {
        return str != null && str.endsWith(".") ? str.substring(0, str.length() - 1) : str;
    }

}