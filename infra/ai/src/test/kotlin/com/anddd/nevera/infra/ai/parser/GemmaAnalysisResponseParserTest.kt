package com.anddd.nevera.infra.ai.parser

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.ai.GemmaGenerationError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GemmaAnalysisResponseParserTest {

    private lateinit var parser: GemmaAnalysisResponseParser

    @BeforeEach
    fun setUp() {
        parser = GemmaAnalysisResponseParser()
    }

    @Test
    fun `정상 JSON을 파싱한다`() {
        val raw = """{"ocrText":"사과 3개","contextSummary":"과일 영수증","ingredients":[{"name":"사과","quantityText":"3개","categoryText":"과일","storageHint":"냉장","confidence":0.9}]}"""
        val result = parser.parse(raw)

        assertTrue(result is NeveraResult.Success)
        val data = (result as NeveraResult.Success).data
        assertEquals("사과 3개", data.ocrText)
        assertEquals("과일 영수증", data.contextSummary)
        assertEquals(1, data.ingredients.size)
        assertEquals("사과", data.ingredients[0].name)
        assertEquals(0.9f, data.ingredients[0].confidence!!, 0.001f)
    }

    @Test
    fun `markdown code fence를 제거하고 파싱한다`() {
        val raw = "```json\n{\"ocrText\":null,\"contextSummary\":null,\"ingredients\":[]}\n```"
        val result = parser.parse(raw)

        assertTrue(result is NeveraResult.Success)
        val data = (result as NeveraResult.Success).data
        assertNull(data.ocrText)
        assertTrue(data.ingredients.isEmpty())
    }

    @Test
    fun `앞뒤에 설명이 섞인 JSON을 파싱한다`() {
        val raw = "다음은 분석 결과입니다:\n{\"ocrText\":\"우유\",\"contextSummary\":null,\"ingredients\":[]}\n분석 완료."
        val result = parser.parse(raw)

        assertTrue(result is NeveraResult.Success)
        assertEquals("우유", (result as NeveraResult.Success).data.ocrText)
    }

    @Test
    fun `ingredients 필드 누락 시 emptyList를 반환한다`() {
        val raw = """{"ocrText":"test","contextSummary":"test"}"""
        val result = parser.parse(raw)

        assertTrue(result is NeveraResult.Success)
        assertTrue((result as NeveraResult.Success).data.ingredients.isEmpty())
    }

    @Test
    fun `confidence가 범위를 벗어나면 coerceIn으로 보정한다`() {
        val raw = """{"ocrText":null,"contextSummary":null,"ingredients":[{"name":"사과","quantityText":null,"categoryText":null,"storageHint":null,"confidence":1.5},{"name":"배","quantityText":null,"categoryText":null,"storageHint":null,"confidence":-0.3}]}"""
        val result = parser.parse(raw)

        assertTrue(result is NeveraResult.Success)
        val ingredients = (result as NeveraResult.Success).data.ingredients
        assertEquals(1.0f, ingredients[0].confidence!!, 0.001f)
        assertEquals(0.0f, ingredients[1].confidence!!, 0.001f)
    }

    @Test
    fun `invalid JSON은 ResponseParseFailed를 반환한다`() {
        val raw = "이것은 JSON이 아닙니다"
        val result = parser.parse(raw)

        assertTrue(result is NeveraResult.Failure)
        assertEquals(GemmaGenerationError.ResponseParseFailed, (result as NeveraResult.Failure).error)
    }

    @Test
    fun `중괄호가 없는 응답은 ResponseParseFailed를 반환한다`() {
        val raw = "오류가 발생했습니다."
        val result = parser.parse(raw)

        assertTrue(result is NeveraResult.Failure)
        assertEquals(GemmaGenerationError.ResponseParseFailed, (result as NeveraResult.Failure).error)
    }

    @Test
    fun `rawText는 항상 원본 텍스트를 보존한다`() {
        val raw = """{"ocrText":null,"contextSummary":null,"ingredients":[]}"""
        val result = parser.parse(raw)

        assertTrue(result is NeveraResult.Success)
        assertEquals(raw, (result as NeveraResult.Success).data.rawText)
    }
}
