package com.kubaice.celltrackai.domain.export

import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent

object CsvExporter {

    fun exportToCsv(culture: CellCulture, events: List<CultureEvent>): String {
        val sb = StringBuilder()
        // RFC 4180 CSV Header
        sb.append("culture_id,event_id,timestamp,event_type,passage_number,confluency_pct,notes,image_count\r\n")

        for (event in events) {
            val cultureId = escapeCsv(culture.id)
            val eventId = escapeCsv(event.id)
            val timestamp = event.occurredAt.toString()
            val eventType = escapeCsv(event.type.name)
            val passageNumber = event.passageNumber?.toString() ?: ""
            val confluencyPct = event.confluencyPercentage?.toString() ?: ""
            val notes = escapeCsv(event.notes ?: "")
            val imageCount = event.imageUris.size.toString()

            sb.append("$cultureId,$eventId,$timestamp,$eventType,$passageNumber,$confluencyPct,$notes,$imageCount\r\n")
        }

        return sb.toString()
    }

    fun escapeCsv(value: String): String {
        if (value.contains("\"") || value.contains(",") || value.contains("\n") || value.contains("\r")) {
            val escaped = value.replace("\"", "\"\"")
            return "\"$escaped\""
        }
        return value
    }
}
