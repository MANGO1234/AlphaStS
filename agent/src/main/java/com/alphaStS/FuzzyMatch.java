package com.alphaStS;

import java.util.ArrayList;
import java.util.List;

public class FuzzyMatch {
    public static record Result(boolean sequential, int score) {}

    public static Result fuzzyMatch(String pattern, String string) {
        // Score consts
        int adjacency_bonus = 5;                // bonus for adjacent matches
        int separator_bonus = 10;               // bonus if match occurs after a separator
        int camel_bonus = 10;                   // bonus if match is uppercase and prev is lower
        // penalty applied for every letter in string before the first match
        int leading_letter_penalty = -3;
        int max_leading_letter_penalty = -9;    // maximum penalty for leading letters
        int unmatched_letter_penalty = -1;      // penalty for every letter that doesn't matter

        // Loop variables
        int score = 0;
        int patternIdx = 0;
        int patternLength = pattern.length();
        int strIdx = 0;
        int strLength = string.length();
        boolean prevMatched = false;
        boolean prevLower = false;
        boolean prevSeparator = true;       // True so if first letter match gets separator bonus

        // Use "best" matched letter if multiple string letters match the pattern
        Character bestLetter = null;
        Character bestLower = null;
        Integer bestLetterIdx = null;
        int bestLetterScore = 0;

        var matchedIndices = new ArrayList<Integer>();

        // Loop over strings
        while (strIdx != strLength) {
            var patternChar = patternIdx != patternLength ? pattern.charAt(patternIdx) : null;
            var strChar = string.charAt(strIdx);

            var patternLower = patternChar != null ? Character.toLowerCase(patternChar) : null;
            var strLower = Character.toLowerCase(strChar);
            var strUpper = Character.toUpperCase(strChar);

            var nextMatch = patternChar != null && patternLower == strLower;
            var rematch = bestLetter != null && bestLower == strLower;

            var advanced = nextMatch && bestLetter != null;
            var patternRepeat = bestLetter != null && patternChar != null && bestLower == patternLower;
            if (advanced || patternRepeat) {
                score += bestLetterScore;
                matchedIndices.add(bestLetterIdx);
                bestLetter = null;
                bestLower = null;
                bestLetterIdx = null;
                bestLetterScore = 0;
            }

            if (nextMatch || rematch) {
                int newScore = 0;

                // Apply penalty for each letter before the first pattern match
                // Note: std::max because penalties are negative values. So max is
                // smallest penalty.
                if (patternIdx == 0) {
                    int penalty = Math.max(strIdx * leading_letter_penalty, max_leading_letter_penalty);
                    score += penalty;
                }

                // Apply bonus for consecutive bonuses
                if (prevMatched) {
                    newScore += adjacency_bonus;
                }

                // Apply bonus for matches after a separator
                if (prevSeparator) {
                    newScore += separator_bonus;
                }

                // Apply bonus across camel case boundaries. Includes "clever"
                // isLetter check.
                if (prevLower && strChar == strUpper && strLower != strUpper) {
                    newScore += camel_bonus;
                }

                // Update patter index IFF the next pattern letter was matched
                if (nextMatch) {
                    patternIdx = patternIdx + 1;
                }

                // Update best letter in string which may be for a "next" letter or a
                // "rematch"
                if (newScore >= bestLetterScore) {
                    // Apply penalty for now skipped letter
                    if (bestLetter != null) {
                        score += unmatched_letter_penalty;
                    }
                    bestLetter = strChar;
                    bestLower = Character.toLowerCase(bestLetter);
                    bestLetterIdx = strIdx;
                    bestLetterScore = newScore;
                }

                prevMatched = true;
            } else {
                // Append unmatch characters
                // formattedStr += strChar
                score += unmatched_letter_penalty;
                prevMatched = false;
            }

            // Includes "clever" isLetter check.
            prevLower = strChar == strLower && strLower != strUpper;
            prevSeparator = strChar == '_' || strChar == ' ';

            strIdx = strIdx + 1;
        }

        // Apply score for last match
        if (bestLetter != null) {
            score += bestLetterScore;
            matchedIndices.add(bestLetterIdx);
        }
        boolean matched = patternIdx == patternLength;
        return new Result(matched, score);
    }

    public static String getBestFuzzyMatch(String query, List<String> strings) {
        String maxString = null;
        int score = Integer.MIN_VALUE;
        for (String string : strings) {
            var r = fuzzyMatch(query, string);
            if (r.score > score && stringContainsLetters(string, query)) {
                maxString = string;
                score = r.score;
            }
        }
        return maxString;
    }

    private static boolean stringContainsLetters(String str, String query) {
        int[] count = new int[256];
        for (int i = 0; i < str.length(); i++) {
            count[str.charAt(i)]++;
        }
        for (int i = 0; i < query.length(); i++) {
            count[query.charAt(i)]--;
            if (count[query.charAt(i)] < 0) {
                return false;
            }
        }
        return true;
    }
}
