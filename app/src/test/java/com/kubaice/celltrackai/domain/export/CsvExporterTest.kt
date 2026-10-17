package com.kubaice.celltrackai.domain.export

import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {

    @Test
    fun testCsvExportHeaderAndEscaping() {
        val culture = CellCulture(
            id = "C-101",
            name = "HeLa Line, \"Batch A\"",
            cellType = "HeLa",
            startedAt = 1700000000000L,
            status = CultureStatus.ACTIVE,
            notes = "Note with comma, and \"quotes\"\nand newline"
        )

        val events = listOf(
            CultureEvent(
                id = "E-001",
                cultureId = "C-101",
                occurredAt = 1700000100000L,
                type = EventType.OBSERVATION,
                passageNumber = 12,
                confluencyPercentage = 80.5f,
                notes = "Healthy monolayer, confluency 80%",
                imageUris = listOf("uri1", "uri2")
            )
        )

        val csvOutput = CsvExporter.exportToCsv(culture, events)

        // Check header
        assertTrue(csvOutput.startsWith("culture_id,event_id,timestamp,event_type,passage_number,confluency_pct,notes,image_count\r\n"))

        // Check event row content
        assertTrue(csvOutput.contains("C-101"))
        assertTrue(csvOutput.contains("E-001"))
        assertTrue(csvOutput.contains("1700000100000"))
        assertTrue(csvOutput.contains("OBSERVATION"))
        assertTrue(csvOutput.contains("12"))
        assertTrue(csvOutput.contains("80.5"))
        assertTrue(csvOutput.contains("\"Healthy monolayer, confluency 80%\""))
        assertTrue(csvOutput.contains(",2\r\n"))
    }

    @Test
    fun testEscapeCsv() {
        assertEquals("SimpleText", CsvExporter.escapeCsv("SimpleText"))
        assertEquals("\"Text with, comma\"", CsvExporter.escapeCsv("Text with, comma"))
        assertEquals("\"Text with \"\"quotes\"\"\"", CsvExporter.escapeCsv("Text with \"quotes\""))
    }
}
