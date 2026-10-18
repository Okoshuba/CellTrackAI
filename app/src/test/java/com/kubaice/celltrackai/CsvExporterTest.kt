package com.kubaice.celltrackai

import com.kubaice.celltrackai.domain.export.CsvExporter
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {

    private val sampleCulture = CellCulture(
        id = "C-101",
        name = "HeLa Line, \"Batch A\"",
        cellType = "HeLa",
        startedAt = 1700000000000L,
        status = CultureStatus.ACTIVE,
        notes = "Note with comma, and \"quotes\"\nand newline"
    )

    @Test
    fun testEscapeCsv() {
        // Plain text remains unchanged
        assertEquals("SimpleText", CsvExporter.escapeCsv("SimpleText"))

        // Comma causes double quote wrapping
        assertEquals("\"Text with, comma\"", CsvExporter.escapeCsv("Text with, comma"))

        // Double quotes are escaped to double-double quotes and wrapped
        assertEquals("\"Text with \"\"quotes\"\"\"", CsvExporter.escapeCsv("Text with \"quotes\""))

        // Newlines cause double quote wrapping
        assertEquals("\"Text with\nnewline\"", CsvExporter.escapeCsv("Text with\nnewline"))
        assertEquals("\"Text with\r\nCRLF\"", CsvExporter.escapeCsv("Text with\r\nCRLF"))
    }

    @Test
    fun testCsvExportHeaderOnlyWhenNoEvents() {
        val csvOutput = CsvExporter.exportToCsv(sampleCulture, emptyList())
        val expectedHeader = "culture_id,event_id,timestamp,event_type,passage_number,confluency_pct,notes,image_count\r\n"
        assertEquals(expectedHeader, csvOutput)
    }

    @Test
    fun testCsvExportSingleEvent() {
        val event = CultureEvent(
            id = "E-001",
            cultureId = "C-101",
            occurredAt = 1700000100000L,
            type = EventType.OBSERVATION,
            passageNumber = 12,
            confluencyPercentage = 80.5f,
            notes = "Healthy monolayer, confluency 80%",
            imageUris = listOf("uri1", "uri2")
        )

        val csvOutput = CsvExporter.exportToCsv(sampleCulture, listOf(event))

        // Verify header
        assertTrue(csvOutput.startsWith("culture_id,event_id,timestamp,event_type,passage_number,confluency_pct,notes,image_count\r\n"))

        // Verify row formatting
        val expectedRow = "C-101,E-001,1700000100000,OBSERVATION,12,80.5,\"Healthy monolayer, confluency 80%\",2\r\n"
        assertTrue(csvOutput.contains(expectedRow))
    }

    @Test
    fun testCsvExportMultipleEvents() {
        val event1 = CultureEvent(
            id = "E-001",
            cultureId = "C-101",
            occurredAt = 1700000100000L,
            type = EventType.MEDIA_CHANGE,
            passageNumber = 10,
            confluencyPercentage = 60.0f,
            notes = "Replaced media, 10ml DMEM",
            imageUris = emptyList()
        )

        val event2 = CultureEvent(
            id = "E-002",
            cultureId = "C-101",
            occurredAt = 1700000200000L,
            type = EventType.PASSAGE,
            passageNumber = 11,
            confluencyPercentage = 90.0f,
            notes = "Split 1:4 with Trypsin",
            imageUris = listOf("uri_a")
        )

        val csvOutput = CsvExporter.exportToCsv(sampleCulture, listOf(event1, event2))

        val lines = csvOutput.split("\r\n").filter { it.isNotEmpty() }
        assertEquals(3, lines.size) // Header + 2 rows

        assertEquals("culture_id,event_id,timestamp,event_type,passage_number,confluency_pct,notes,image_count", lines[0])
        assertEquals("C-101,E-001,1700000100000,MEDIA_CHANGE,10,60.0,\"Replaced media, 10ml DMEM\",0", lines[1])
        assertEquals("C-101,E-002,1700000200000,PASSAGE,11,90.0,Split 1:4 with Trypsin,1", lines[2])
    }
}
