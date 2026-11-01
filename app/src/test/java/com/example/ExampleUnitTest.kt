package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCoreTechQuestions() {
        val aiQuery = "what is ai"
        assertTrue(aiQuery.contains("ai") || aiQuery.contains("artificial intelligence"))

        val computerQuery = "computer kya hai"
        assertTrue(computerQuery.contains("computer"))
    }

    @Test
    fun testTeachingExtractionPatterns() {
        val input = "Buny seekh lo: python ka matlab programming language hota hai"
        val teachKeywords = listOf("seekh lo", "seekho", "yaad rakhna", "yaad rakho")
        val matched = teachKeywords.any { input.lowercase().contains(it) }
        assertTrue(matched)

        val parts = input.split("ka matlab")
        assertEquals(2, parts.size)
        val q = parts[0].replace("Buny seekh lo:", "").trim()
        val a = parts[1].replace("hota hai", "").trim()
        assertEquals("python", q)
        assertEquals("programming language", a)
    }

    @Test
    fun testMathCalculationAccuracy() {
        val n1 = 25.0
        val n2 = 84.0
        val result = n1 * n2
        assertEquals(2100.0, result, 0.001)
    }
}
