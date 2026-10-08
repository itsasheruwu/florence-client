/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.search;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuzzyMatcherTest {
    @Test
    void lettersOutOfOrderOrMissingDontMatch() {
        assertFalse(FuzzyMatcher.match("ea", "Auto").matched());
        assertFalse(FuzzyMatcher.match("xyz", "Auto Eat").matched());
        assertFalse(FuzzyMatcher.match("autoeatt", "Auto Eat").matched());
    }

    @Test
    void anEmptyQueryMatchesEverything() {
        assertTrue(FuzzyMatcher.match("", "Auto Eat").matched());
        assertTrue(FuzzyMatcher.match("   ", "Auto Eat").matched());
        assertEquals(0, FuzzyMatcher.score("", "anything"));
    }

    @Test
    void matchingIgnoresCase() {
        assertTrue(FuzzyMatcher.match("AUTO", "auto eat").matched());
        assertTrue(FuzzyMatcher.match("auto", "AUTO EAT").matched());
    }

    @Test
    void lettersCanBeSpreadOut() {
        assertTrue(FuzzyMatcher.match("ae", "Auto Eat").matched());
        assertTrue(FuzzyMatcher.match("aeat", "Auto Eat").matched());
    }

    @Test
    void startsOfWordsScoreHigherThanLettersInTheMiddle() {
        // The same two letters, at the start of both words and in the middle of one
        assertTrue(FuzzyMatcher.score("ae", "Auto Eat") > FuzzyMatcher.score("ae", "Chat Edit Plus"));
        assertTrue(FuzzyMatcher.score("ae", "Auto Eat") > FuzzyMatcher.score("ae", "Fireball"));
    }

    @Test
    void prefixesScoreHigherThanMatchesFurtherIn() {
        assertTrue(FuzzyMatcher.score("aur", "Aura") > FuzzyMatcher.score("aur", "Anchor Aura"));
        assertTrue(FuzzyMatcher.score("aur", "Aura") > FuzzyMatcher.score("aur", "Kill Aura Plus"));
    }

    @Test
    void exactMatchesBeatLongerOnes() {
        assertTrue(FuzzyMatcher.score("fly", "Fly") > FuzzyMatcher.score("fly", "Fly Plus"));
        assertTrue(FuzzyMatcher.score("fly", "Fly Plus") > FuzzyMatcher.score("fly", "Elytra Fly"));
    }

    @Test
    void letters_that_run_together_beat_letters_that_dont() {
        assertTrue(FuzzyMatcher.score("kill", "Killaura") > FuzzyMatcher.score("kill", "Knockback Leave Lag"));
    }

    @Test
    void everyWordOfAQueryHasToMatch() {
        assertTrue(FuzzyMatcher.match("auto eat", "Auto Eat").matched());
        assertTrue(FuzzyMatcher.match("eat auto", "Auto Eat").matched());
        assertFalse(FuzzyMatcher.match("auto eat", "Auto Armor").matched());
    }

    @Test
    void positionsPointAtTheMatchedLetters() {
        FuzzyMatcher.Result result = FuzzyMatcher.match("ae", "Auto Eat");

        assertArrayEquals(new int[] {0, 5}, result.positions());
    }

    @Test
    void positionsOfSeveralWordsAreCombinedInOrder() {
        FuzzyMatcher.Result result = FuzzyMatcher.match("eat auto", "Auto Eat");

        assertArrayEquals(new int[] {0, 1, 2, 3, 5, 6, 7}, result.positions());
    }

    @Test
    void theBestWayToMatchIsFound() {
        // The first 'a' is at the start but the better match is the word that starts with it
        FuzzyMatcher.Result result = FuzzyMatcher.match("ba", "Baritone Auto Aim");

        assertTrue(result.matched());
        assertEquals(0, result.positions()[0]);
    }

    @Test
    void sortingByScorePutsTheBestFirst() {
        List<String> names = List.of("Chat Edit Plus", "Auto Eat", "Fireball", "Autoeat", "Automatic Eating Timer");

        List<String> sorted = names.stream()
            .filter(name -> FuzzyMatcher.match("auto eat", name).matched())
            .sorted(Comparator.comparingInt((String name) -> FuzzyMatcher.score("auto eat", name)).reversed())
            .toList();

        assertEquals("Auto Eat", sorted.get(0));
        assertFalse(sorted.contains("Fireball"));
    }

    @Test
    void nullsDontMatch() {
        assertFalse(FuzzyMatcher.match(null, "x").matched());
        assertFalse(FuzzyMatcher.match("x", null).matched());
    }

    @Test
    void aQueryLongerThanTheTargetDoesntMatch() {
        assertFalse(FuzzyMatcher.match("auto eat plus", "Auto").matched());
    }
}
