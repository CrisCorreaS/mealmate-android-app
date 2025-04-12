package com.cristina.correa.mealmatecristina.utils;

/**
 * A utility class that provides methods for working with descriptions.
 * It includes functionality to truncate a description based on specified character and word limits.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class DescriptionUtils {

    /**
     * Truncates a description if it exceeds the specified limits.
     *
     * @param description The original description.
     * @param maxChars    Maximum number of characters allowed (including "...").
     * @param maxWords    Maximum number of words allowed.
     * @return The truncated description with "..." if the limits are exceeded.
     */
    public static String truncateDescription(String description, int maxChars, int maxWords) {
        if (description == null || description.isEmpty()) {
            return "";
        }

        String[] words = description.split(" ");
        StringBuilder truncated = new StringBuilder();

        int charCount = 0;
        int wordCount = 0;

        for (String word : words) {
            if (charCount + word.length() + (wordCount > 0 ? 1 : 0) > maxChars || wordCount + 1 > maxWords) {
                break;
            }

            if (wordCount > 0) {
                truncated.append(" ");

                charCount++;
            }

            truncated.append(word);
            charCount += word.length();
            wordCount++;
        }

        if (truncated.length() < description.length()) {
            int lastCharIndex = truncated.length() - 1;

            if (truncated.charAt(lastCharIndex) == ',') {
                truncated.deleteCharAt(lastCharIndex);
            }

            truncated.append("...");
        }

        return truncated.toString();
    }
}

