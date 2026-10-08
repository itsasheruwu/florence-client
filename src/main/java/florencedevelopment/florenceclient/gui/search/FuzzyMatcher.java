/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.search;

import java.util.Arrays;

/**
 * Matches what the user typed against names the way editors do: the letters only have to appear in order, and matches
 * that start words or run together score higher. "ae" finds "Auto Eat" and ranks it above "Anchor Aura Extra".
 * <p>
 * Based on the algorithm described in https://www.forrestthewoods.com/blog/reverse_engineering_sublime_texts_fuzzy_match/
 */
public final class FuzzyMatcher {
    /** Returned when the query doesn't match. */
    public static final int NO_MATCH = Integer.MIN_VALUE;

    private static final int MAX_MATCHES = 256;
    private static final int RECURSION_LIMIT = 10;

    private static final int SEQUENTIAL_BONUS = 15;
    private static final int SEPARATOR_BONUS = 30;
    private static final int CAMEL_BONUS = 30;
    private static final int FIRST_LETTER_BONUS = 15;
    private static final int LEADING_LETTER_PENALTY = -5;
    private static final int MAX_LEADING_LETTER_PENALTY = -15;
    private static final int UNMATCHED_LETTER_PENALTY = -1;

    // Added when the target starts with the query or equals it
    private static final int PREFIX_BONUS = 40;
    private static final int EXACT_BONUS = 40;

    private FuzzyMatcher() {}

    /**
     * The result of a match.
     *
     * @param score     higher is better, {@link #NO_MATCH} if there was no match
     * @param positions indexes of the characters of the target that were matched, to highlight them
     */
    public record Result(int score, int[] positions) {
        public boolean matched() {
            return score != NO_MATCH;
        }
    }

    private static final Result NOTHING = new Result(NO_MATCH, new int[0]);

    /**
     * Matches a query that can be several words, every word has to match somewhere in the target.
     */
    public static Result match(String query, String target) {
        if (query == null || target == null) return NOTHING;

        String trimmed = query.trim();
        if (trimmed.isEmpty()) return new Result(0, new int[0]);

        int total = 0;
        int[] positions = new int[0];

        for (String word : trimmed.split("\\s+")) {
            Result result = matchWord(word, target);
            if (!result.matched()) return NOTHING;

            total += result.score();
            positions = merge(positions, result.positions());
        }

        return new Result(total, positions);
    }

    public static int score(String query, String target) {
        return match(query, target).score();
    }

    private static Result matchWord(String word, String target) {
        if (word.length() > target.length() || word.length() > MAX_MATCHES) return NOTHING;

        int[] matches = new int[MAX_MATCHES];
        int[] count = new int[1];
        int[] recursionCount = new int[1];
        int[] score = new int[1];

        boolean matched = recurse(word, 0, target, 0, score, null, 0, matches, count, recursionCount);

        if (!matched) return NOTHING;

        int total = score[0];

        if (startsWithIgnoreCase(target, word)) total += PREFIX_BONUS;
        if (target.length() == word.length() && startsWithIgnoreCase(target, word)) total += EXACT_BONUS;

        return new Result(total, Arrays.copyOf(matches, count[0]));
    }

    private static boolean startsWithIgnoreCase(String target, String prefix) {
        return target.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * @param srcMatches matches found so far by the caller, or null
     * @param srcCount   how many of them there are
     * @param matches    receives the matches of this call and the ones it continued from
     * @param count      receives how many matches there are
     */
    private static boolean recurse(String pattern, int patternIndex, String str, int strIndex, int[] outScore,
                                   int[] srcMatches, int srcCount, int[] matches, int[] count, int[] recursionCount) {
        recursionCount[0]++;
        if (recursionCount[0] >= RECURSION_LIMIT) return false;

        if (patternIndex >= pattern.length() || strIndex >= str.length()) return false;

        int nextMatch = srcCount;
        if (srcMatches != null) System.arraycopy(srcMatches, 0, matches, 0, srcCount);

        boolean recursiveMatch = false;
        int[] bestRecursiveMatches = new int[MAX_MATCHES];
        int[] bestRecursiveCount = new int[1];
        int bestRecursiveScore = 0;

        int p = patternIndex;
        int s = strIndex;

        while (p < pattern.length() && s < str.length()) {
            if (Character.toLowerCase(pattern.charAt(p)) == Character.toLowerCase(str.charAt(s))) {
                if (nextMatch >= MAX_MATCHES) return false;

                // Try again with this match skipped
                int[] recursiveMatches = new int[MAX_MATCHES];
                int[] recursiveCount = new int[1];
                int[] recursiveScore = new int[1];

                if (recurse(pattern, p, str, s + 1, recursiveScore, matches, nextMatch, recursiveMatches, recursiveCount, recursionCount)) {
                    if (!recursiveMatch || recursiveScore[0] > bestRecursiveScore) {
                        System.arraycopy(recursiveMatches, 0, bestRecursiveMatches, 0, MAX_MATCHES);
                        bestRecursiveCount[0] = recursiveCount[0];
                        bestRecursiveScore = recursiveScore[0];
                    }

                    recursiveMatch = true;
                }

                matches[nextMatch++] = s;
                p++;
            }

            s++;
        }

        boolean matched = p >= pattern.length();

        if (!matched) return false;

        // Score this match
        int score = 100;

        int penalty = LEADING_LETTER_PENALTY * matches[0];
        if (penalty < MAX_LEADING_LETTER_PENALTY) penalty = MAX_LEADING_LETTER_PENALTY;
        score += penalty;

        score += UNMATCHED_LETTER_PENALTY * (str.length() - nextMatch);

        for (int i = 0; i < nextMatch; i++) {
            int current = matches[i];

            if (i > 0 && current == matches[i - 1] + 1) score += SEQUENTIAL_BONUS;

            if (current > 0) {
                char neighbor = str.charAt(current - 1);
                char c = str.charAt(current);

                if (Character.isLowerCase(neighbor) && Character.isUpperCase(c)) score += CAMEL_BONUS;
                if (neighbor == '_' || neighbor == ' ' || neighbor == '-') score += SEPARATOR_BONUS;
            }
            else {
                score += FIRST_LETTER_BONUS;
            }
        }

        if (recursiveMatch && bestRecursiveScore > score) {
            System.arraycopy(bestRecursiveMatches, 0, matches, 0, MAX_MATCHES);
            count[0] = bestRecursiveCount[0];
            outScore[0] = bestRecursiveScore;
        }
        else {
            count[0] = nextMatch;
            outScore[0] = score;
        }

        return true;
    }

    private static int[] merge(int[] a, int[] b) {
        if (a.length == 0) return b;

        int[] merged = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, merged, a.length, b.length);
        Arrays.sort(merged);

        // Drop duplicates, words can overlap
        int unique = 0;
        for (int i = 0; i < merged.length; i++) {
            if (i == 0 || merged[i] != merged[i - 1]) merged[unique++] = merged[i];
        }

        return Arrays.copyOf(merged, unique);
    }
}
