package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ScrabbleRulesTest {

    @Test
    fun testVowelsAndCommonLetters_1_point() {
        val chars = listOf('А', 'В', 'Е', 'И', 'І', 'Н', 'О', 'Р', 'Т', 'а', 'н', 'т')
        chars.forEach {
            assertEquals("Expected 1 for $it", 1, ScrabbleRules.getLetterBaseValue(it))
        }
    }

    @Test
    fun testConsonants_2_points() {
        val chars = listOf('Д', 'К', 'Л', 'М', 'П', 'С', 'д', 'л', 'с')
        chars.forEach {
            assertEquals("Expected 2 for $it", 2, ScrabbleRules.getLetterBaseValue(it))
        }
    }

    @Test
    fun testLetterU_3_points() {
        assertEquals(3, ScrabbleRules.getLetterBaseValue('У'))
        assertEquals(3, ScrabbleRules.getLetterBaseValue('у'))
    }

    @Test
    fun testLetters_4_points() {
        val chars = listOf('Б', 'Г', 'З', 'Я', 'б', 'я')
        chars.forEach {
            assertEquals("Expected 4 for $it", 4, ScrabbleRules.getLetterBaseValue(it))
        }
    }

    @Test
    fun testLetters_5_points() {
        val chars = listOf('Й', 'Х', 'Ч', 'Ь', 'й', 'ь')
        chars.forEach {
            assertEquals("Expected 5 for $it", 5, ScrabbleRules.getLetterBaseValue(it))
        }
    }

    @Test
    fun testLetters_6_points() {
        val chars = listOf('Ж', 'Ї', 'Ц', 'Ш', 'ж', 'ц')
        chars.forEach {
            assertEquals("Expected 6 for $it", 6, ScrabbleRules.getLetterBaseValue(it))
        }
    }

    @Test
    fun testLetterYu_7_points() {
        assertEquals(7, ScrabbleRules.getLetterBaseValue('Ю'))
        assertEquals(7, ScrabbleRules.getLetterBaseValue('ю'))
    }

    @Test
    fun testLetters_8_points() {
        val chars = listOf('Є', 'Ф', 'Щ', 'є', 'щ')
        chars.forEach {
            assertEquals("Expected 8 for $it", 8, ScrabbleRules.getLetterBaseValue(it))
        }
    }

    @Test
    fun testApostropheAndGe_10_points() {
        val chars = listOf('\'', 'Ґ', 'ґ')
        chars.forEach {
            assertEquals("Expected 10 for $it", 10, ScrabbleRules.getLetterBaseValue(it))
        }
    }

    @Test
    fun testInvalidOrForeignCharacters_0_points() {
        val chars = listOf('Z', 'x', '1', '!', ' ', '@')
        chars.forEach {
            assertEquals("Expected 0 for $it", 0, ScrabbleRules.getLetterBaseValue(it))
        }
    }
}
